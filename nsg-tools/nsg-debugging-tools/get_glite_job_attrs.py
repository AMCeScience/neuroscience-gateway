"""
Copyright (C) 2013 Academic Medical Center of the University of Amsterdam
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""
 

#!/usr/bin/env python

import re
import os
import sys
import time
#import datetime
import pprint
import logging
import getpass
import copy
from optparse import OptionParser

## load json modules for output formatting
try:
    import json
except ImportError:
    import simplejson as json

## load xml modules (use elementtree: http://effbot.org/zone/element-index.htm)
sys.path.append(os.path.dirname(os.path.abspath(__file__))+'/external/lib/python')
import elementtree.ElementTree as ET
guse_logg_xlns='{http://dcibridge.sztaki.lpds.hu/schema/loggXmlSchema}'

## load utility modules
sys.path.append(os.path.dirname(os.path.abspath(__file__)))
from utils.Common import *
from utils.Shell import *

class GuseJobInfo:
    '''
    data object for storing gUse job information.
     - i.e. each "item" in the guse.logg file
    '''

    def __init__(self, id=''):
        self.name    = '' 
        self.time    = None 
        self.etc     = 0 
        self.level   = 0 
        self.info    = '' 

    def __str__(self):
        return pp.pformat(self.__dict__)

    def __repr__(self):
        return repr(self.__dict__)

    def setInfo(self,key,value):
        setattr(self, key, value)

    def update(self, other):
        if not isinstance(other, GLiteJobInfo):
            raise NotImplementedError
        self.__dict__ = copy.deepcopy(other.__dict__)

class GLiteJobInfo:
    '''
    data object for storing gLite job information.
     - i.e. data parsed from the glite-wms-job-status -v 3 command 
    '''

    def __init__(self, id=''):
        self.id          = id
        self.name        = '' 
        self.history     = {}
        self.is_node     = False 
        self.status      = '' 
        self.ec_glite    = None
        self.ec_wrapper  = None
        self.reason      = ''
        self.dest_ce     = ''
        self.dest_wn     = ''
        self.dest_wn_sys = ''

    def __eq__(self, other):
        if not isinstance(other, GLiteJobInfo):
            raise NotImplementedError
        return self.id == other.id

    def __str__(self):
        return pp.pformat(self.__dict__)

    def __repr__(self):
        return repr(self.__dict__)

    def setInfo(self,key,value):
        setattr(self, key, value)

    def setHistory(self,key,value):
        self.history[key] = makeStructTimeUTC(value)

    def update(self, other):
        if not isinstance(other, GLiteJobInfo):
            raise NotImplementedError

        for k,v in other.__dict__.items():
            if not v:
                ## drop attributes with empty value in other.__dict__
                del other.__dict__[k]
            elif type(v) is dict:
                ## merge attributes with dict structure 
                self.__dict__[k].update(other.__dict__[k])
                del other.__dict__[k]
            elif type(v) is list:
                ## merge attributes with list structure 
                self.__dict__[k] += other.__dict__[k]
                del other.__dict__[k]
            else:
                pass

        ## update other attributes in self.__dict__
        self.__dict__.update(other.__dict__)

    #### static methods ####
    def fromList(id, infoList, createNewObj=True):
        '''
        retrieve the GLiteJobInfo object from infoList with the given glite job id
        '''
        tmp_obj = GLiteJobInfo(id=id)
        try:
            return infoList[ infoList.index( tmp_obj ) ]
        except ValueError, e:
            if createNewObj:
                return tmp_obj
            else:
                return None

    fromList = staticmethod( fromList )

def report(guse_info_list,format='plain'):
    '''
    - create human readable report given guse_info. 
    '''

    def __report_plain__():  ## output in plain format
        for guse_info in guse_info_list:
 
            ## print dictionary in pretty print format
            #pp = pprint.PrettyPrinter(indent=4)
            #pp.pprint(guse_info)
 
            print '=' * 32
            print 'uid   : %s' % guse_info['uid']
            print 'wfname: %s' % guse_info['wfname']
            print '=' * 32
            for dir in sorted(guse_info['jobs'].keys()):
                print '  |- dir    : %s' % dir
                print '  |- start  : %s' % fmtStructTimeUTC( guse_info['jobs'][dir]['t_beg'] )
                print '  |- finish : %s' % fmtStructTimeUTC( guse_info['jobs'][dir]['t_end'] )
                print '  |- failed : %s' % repr( guse_info['jobs'][dir]['is_failed'] )
                print '  |- attemps:'
                for jinfo in guse_info['jobs'][dir]['glite']:
                    print '     |- %s (%s)' % (jinfo.id, jinfo.status)
            print ''

    def __encode_GLiteJobInfo_json__(obj):
        '''
        special json encoder for GLiteJobInfo object
        '''
        if isinstance(obj,GLiteJobInfo):
            return obj.__dict__
        elif isinstance(obj,time.struct_time):
            return fmtStructTimeUTC(obj)
        else:
            return json.dumps(obj)

    def __report_json__(): ## output in json format
        print json.dumps(guse_info_list,
                         default = __encode_GLiteJobInfo_json__,
                         indent  = 4,
                         separators = (',',':'))

    ## switch between formats
    if format == 'json':
        __report_json__()
    else:
        __report_plain__()

def get_job_info(jobids):
    '''
    - retrieve gLite job info given the job ids
    '''

    def __resolve_gridcmd_log_path__(regxp_logfname, cmd_output):
        match_log = re.search(regxp_logfname,cmd_output)

        logfile = None
        if match_log:
            logfile = match_log.group(1)
        return logfile

    def __print_gridcmd_log__(regxp_logfname,cmd_output):

        logfile = __resolve_gridcmd_log_path__(regxp_logfname,cmd_output)

        if logfile:
            f = open(logfile,'r')
            for l in f.readlines():
                logger.warning(l.strip())
            f.close()

            ## here we assume the logfile is no longer needed at this point - remove it
            os.remove(logfile)
        else:
            logger.warning('output\n%s\n',cmd_output)
            logger.warning('end of output')   

    def __resolve_no_matching_jobs__(cmd_output):
        '''Parsing the glite-wms-job-status log to get the glite jobs which have been removed from the WMS'''

        logfile = __resolve_gridcmd_log_path__('(.*-job-status.*\.log)', cmd_output)

        glite_ids = []

        if logfile:

            f = open(logfile,'r')
            output = f.readlines()
            f.close()

            re_jid = re.compile('^Unable to retrieve the status for: (https:\/\/\S+:9000\/[0-9A-Za-z_\.\-]+)\s*$')
            re_key = re.compile('^.*(no matching jobs found|410 Gone|matching job already purged)\s*$')
            
            m_jid = None
            m_key = None
            myjid = ''
            for line in output:
                m_jid = re_jid.match(line)
                if m_jid:
                    myjid = m_jid.group(1)
                    m_jid = None

                if myjid:
                    m_key = re_key.match(line)
                    if m_key:
                        glite_ids.append(myjid)
                        myjid = ''

        return glite_ids

    def __clean_gridcmd_log__(regxp_logfname, cmd_output):

        logfile = __resolve_gridcmd_log_path__(regxp_logfname,cmd_output)

        if logfile and os.path.exists(logfile):
            os.remove(logfile)

        return True

    ## write jids into a temporary file
    idsfile = tempfile.mktemp('.jids')
    file(idsfile,'w').write('\n'.join(jobids)+'\n')

    cmd = 'glite-wms-job-status --noint -v 3 -i %s' % idsfile

    logger.debug('job status command: %s' % cmd)

    ## make a shell object and call the glite-wms command
    s = Shell(debug=False)
    rc, output, m = s.cmd1(cmd, allowed_exit=[0,255], timeout=300)
    os.remove(idsfile)

    missing_glite_jids = []
    if rc != 0:
        missing_glite_jids = __resolve_no_matching_jobs__(output)

        if missing_glite_jids:
            logger.info('some jobs removed from WMS. Mark them with status "Removed".')
            logger.debug('jobs removed from WMS: %s' % repr(missing_glite_jids))
        else:
            __print_gridcmd_log__('(.*-job-status.*\.log)',output)

    ## parsing the glite-wms-job-status output
    re_id = re.compile('^\s*Status info for the Job : (https://.*\S)\s*$')
    re_status  = re.compile('^\s*Current Status:\s+(.*\S)\s*$')
    re_tsubmit = re.compile('^\s*Submitted:\s+(.*\S)\s*$')

    ## from glite UI version 1.5.14, the attribute 'Node Name:' is no longer available
    ## for distinguishing main and node jobs. A new way has to be applied.
    #re_name = re.compile('^\s*Node Name:\s+(.*\S)\s*$')
    re_exit = re.compile('^\s*Exit code:\s+(.*\S)\s*$')
    re_reason = re.compile('^\s*Status Reason:\s+(.*\S)\s*$')
    re_dest = re.compile('^\s*Destination:\s+(.*\S)\s*$')

    ## pattern to distinguish main and node jobs
    re_main = re.compile('^BOOKKEEPING INFORMATION:\s*$')
    re_node   = re.compile('^- Nodes information.*\s*$')

    ## pattern for node jobs (only meaninful for gLite bulk job submission)
    re_nodename = re.compile('^\s*NodeName\s*=\s*"(gsj_[0-9]+)";\s*$')

    ## generic way to retrieve detailed information of the job 
    pat_attrs = {'dest_wn':'Ce node','exit':'Done code'}
    pat_times = {'last_update':'Lastupdatetime'}

    info = []
    is_main = False
    is_node   = False
    #node_cnt  = 0
    for line in output.split('\n'):

        match = re_main.match(line)
        if match:
            is_main = True
            is_node   = False
            #node_cnt  = 0
            continue

        match = re_node.match(line)
        if match:
            is_main = False
            is_node   = True
            continue

        match = re_id.match(line)
        if match:
            info += [ GLiteJobInfo(id = match.group(1)) ]

            if is_node:
                info[-1].is_node = True
            #if is_node:
            #    info[-1]['name'] = 'node_%d' % node_cnt
            #    node_cnt = node_cnt + 1
            continue

        match = re_nodename.match(line)
        if match and is_node:
            info[-1].name = match.group(1)
            #logger.debug('id: %s, name: %s' % (info[-1]['id'],info[-1]['name']))
            continue

        match = re_status.match(line)
        if match:
            info[-1].status = match.group(1)
            continue

        match = re_exit.match(line)
        if match:
            info[-1].ec_glite = match.group(1)
            continue

        match = re_reason.match(line)
        if match:
            info[-1].reason = match.group(1)
            continue

        match = re_dest.match(line)
        if match:
            info[-1].dest_ce = match.group(1)
            continue

        match = re_tsubmit.match(line)
        if match:
            info[-1].setHistory('submitted', match.group(1))
            continue

        for k,v in pat_attrs.iteritems():
            re_pat = re.compile('^\-\s*%s\s*=\s*(.*\S)\s*$' % v)
            match = re_pat.match(line)
            if match:
                info[-1].setInfo(k, match.group(1))
                break
            else:
                continue

        for k,v in pat_times.iteritems():
            re_pat = re.compile('^\-\s*%s\s*=\s*(.*\S)\s*$' % v)
            match = re_pat.match(line)
            if match:
                info[-1].setHistory(k, match.group(1))
                break
            else:
                continue

    logger.info( 'glite job info:\n%s' % pp.pformat(info) )

    return (info, missing_glite_jids)

def qryUserId(uid):
    '''
    - query the liferay database to convert screenName to internal user id. 
    '''

    id = uid
    mysql_pass = None

    if sys.stdin.isatty(): ## for interactive password typing
        mysql_pass = getpass.getpass('Password of MySQL DB: ')
    else: ## for pipeing-in password
        print 'Password of MySQL DB: '
        mysql_pass = sys.stdin.readline().rstrip()

    conn = getMySQLConnector(uid='root', passwd=mysql_pass, db='neuroscience')

    if conn:
        qry  = "SELECT `LiferayID` FROM `User` WHERE `UserID` LIKE '%" + uid + "%'"
        logger.debug('performing query: %s' % qry)
        cur = conn.cursor()
        cur.execute(qry)
 
        if cur.rowcount == 1: ## we expect only one use id
            id = cur.fetchone()[0]
            logger.debug('get user id: %s' % id)
        else:
            logger.warning('find %d ids for the screenName %s' % (cur.rowcount, uid))

        conn.close()

    return id

def qryWorkflowToProcessFromProvDB():
    '''
    query the provenance database to get Workflow Activities with StartTime == NULL 
    '''

    wfs = {}

    cnx = getMySQLConnector(uid=provdb_user, passwd=provdb_pass, db=provdb_name)

    if cnx:
        crs = cnx.cursor()
        qry = ('SELECT `ActivityKey`,`ActivityId` FROM `Activity` WHERE `EndTime` is NULL')
        crs.execute(qry)
        logger.info('=== %d workflows to check ===' % crs.rowcount )
        for (key,id) in crs:
            logger.info('key: %s id: %s' % (key,id))
            wfs[key] = id
        logger.info('======')
        crs.close()
        cnx.close()

    return wfs

def updateProvDB( prov_wfs, guse_info ):
    '''
    insert job attributes into the provenance database
    '''

    qry1  = 'INSERT INTO ActivityAttributes (ActivityKey, AttributesKey, AttributesValue) VALUES (%s,%s,%s) '
    qry1 += 'ON DUPLICATE KEY UPDATE AttributesValue=VALUES(AttributesValue)'
    qry2  = 'UPDATE Activity SET StartTime=%s, EndTime=%s WHERE ActivityKey=%s'
    qry3  = 'UPDATE Activity SET StartTime=%s WHERE ActivityKey=%s'
    data1 = []
    data2 = []
    data3 = []

    for info in guse_info:
        wfn = info['wfname']

        if not info['jobs']:
            logger.warning('cannot find job attributes: %s' % wfn)
            continue

        logger.debug('insert attributes for %s ...' % wfn)

        key = None
        for k,v in prov_wfs.items():
            if v == wfn: key = k

        ts_max = time.strptime('Jan 1 00:00:00 2100', '%b %d %H:%M:%S %Y')
        ts_min = 0

        wf_beg = ts_max
        wf_end = ts_min

        if key:

            logger.debug( 'workflow: %s %s'  % (key,wfn) )

            ## number of expected jobs of the workflow
            n_jobs = len( info['jobs'] )
            data1.append((key,"Number of jobs",str(n_jobs)))

            i = 0
            for dir in sorted(info['jobs'].keys()):
                i += 1

                ## use job-level start/finish time to update the workflow start/finish time
                t = info['jobs'][dir]['t_beg']
                if t and t < wf_beg: wf_beg = t
                 
                t = info['jobs'][dir]['t_end']
                if t and t > wf_end: wf_end = t

                ## number of attemps per job 
                n_attempt = len( info['jobs'][dir]['glite'] )
                data1.append((key,"Job %d attempts" % i, str(n_attempt)))
                data1.append((key,"Job %d workdir" % i, dir))
                data1.append((key,"Job %d failed" % i, repr(info['jobs'][dir]['is_failed'])))

                j = 0
                for jinfo in info['jobs'][dir]['glite']:
                    j += 1
                    if jinfo.id:          data1.append((key,"ID (%d:%d)" % (i,j)    , jinfo.id))
                    if jinfo.status:      data1.append((key,"Status (%d:%d)" % (i,j), jinfo.status))
                    if jinfo.dest_ce:     data1.append((key,"Computing Element (%d:%d)" % (i,j)    , jinfo.dest_ce))
                    if jinfo.dest_wn:     data1.append((key,"Worker Node (%d:%d)" % (i,j)    , jinfo.dest_wn))
                    if jinfo.dest_wn_sys: data1.append((key,"OS (%d:%d)" % (i,j)   , jinfo.dest_wn_sys.replace(jinfo.dest_wn,'') ))
                    if jinfo.ec_wrapper:  data1.append((key,"Exit Code (%d:%d)" % (i,j)    , jinfo.ec_wrapper))

                    try:
                        t = jinfo.history['submitted']
                        data1.append((key,"time submit (%d:%d)" % (i,j),
                                    time.strftime('%a %b %d %H:%M:%S %Y', t)))

                        if t < wf_beg: wf_beg = t
                    except KeyError,e:
                        pass

                    try:
                        t = jinfo.history['failed']
                        data1.append((key,"time failed (%d:%d)" % (i,j),
                                    time.strftime('%a %b %d %H:%M:%S %Y', t)))
                        if t > wf_end: wf_end = t
                    except KeyError,e:
                        pass

                    try:
                        t = jinfo.history['exec_beg']
                        data1.append((key,"time start (%d:%d)" % (i,j),
                                    time.strftime('%a %b %d %H:%M:%S %Y', t)))
                        if t < wf_beg: wf_beg = t
                    except KeyError,e:
                        pass

                    try:
                        t = jinfo.history['exec_end']
                        data1.append((key,"time finish (%d:%d)" % (i,j),
                                    time.strftime('%a %b %d %H:%M:%S %Y', t)))
                        if t > wf_end: wf_end = t
                    except KeyError,e:
                        pass

                    try:
                        t = jinfo.history['last_update']
                        data1.append((key,"time last update (%d:%d)" % (i,j),
                                    time.strftime('%a %b %d %H:%M:%S %Y', t)))
                        if t > wf_end: wf_end = t
                    except KeyError,e:
                        pass

            try:
                if wf_beg == ts_max:    ## workflow start time not found
                    logger.warning('workflow start time not found: %s' % wfn)
                else:
                    logger.debug( 'approximate start time: %s'  % time.strftime('%a %b %d %H:%M:%S %Y', wf_beg) )
                    if wf_end == ts_min:  ## workflow finish time not found
                        data3.append((time.strftime('%Y-%m-%d %H:%M:%S', wf_beg), key))
                        logger.warning('workflow finish time not found: %s' % wfn)
                    else:
                        data2.append((time.strftime('%Y-%m-%d %H:%M:%S', wf_beg),
                                      time.strftime('%Y-%m-%d %H:%M:%S', wf_end), key))
                        logger.debug( 'approximate finish time: %s' % time.strftime('%a %b %d %H:%M:%S %Y', wf_end) )

            except Exception, e:
                logger.warning( 'cannot resolve StartTime or EndTime: %s %s %s' % (repr(wf_beg), repr(wf_end), wfn) )

    logger.debug(qry1)
    for d in data1:
        logger.debug(d)

    logger.debug(qry2)
    for d in data2:
        logger.debug(d)

    logger.debug(qry3)
    for d in data3:
        logger.debug(d)

    if data1:
        ## insert data into the Activity and ActivityAttributes tables
        cnx = getMySQLConnector(uid=provdb_user, passwd=provdb_pass, db=provdb_name)
        if cnx:
            crs = None
            try:
                ## insert/update ActivityAttributes table with job attributes
                ##data1 = map(lambda x:x+(x[-1],), data1)  ## extend the data tuple for the UPDATE clause

                crs = cnx.cursor()
                crs.executemany(qry1,data1)
 
                ## update Activity table for workflows with both start and finish time detected
                if data2:
                    crs.executemany(qry2,data2)

                ## update Activity table for workflows with only the start time detected
                if data3:
                    crs.executemany(qry3,data3)

                cnx.commit()
 
            except Exception,e:
                logger.error(e)
            else:
                if crs: crs.close()
                if cnx: cnx.close()

def find_wn_info(logw):
    '''
    - parse guse job wrapper output (gridnfo.log)
    - retrieve exec start time from line, e.g. "- Exe started at          : Wed Oct 30 16:07:51 CET 2013"
    - retrieve exec finish time from line, e.g. "- Exe finished at         : Wed Oct 30 16:07:51 CET 2013"
    - retrieve exec exit code from line, e.g. "- The exit code of the exe: 0"
    - retrieve grid job id from line, e.g. "(GLITE_WMS|GRID)_JOBID=https://grasveld.nikhef.nl:9000/GdCROwAJacDq-jz1w2suUA"
    - retrieve CE id from line, e.g. "CE_ID=cygnus.grid.rug.nl:8443/cream-pbs-short"
    - retrieve hostname from line, e.g. "- Ran on host (hostname)  : musca051"
    - retrieve system info from line, e.g. "- Host info-s (uname -a)  : Linux musca051 2.6.32-358.11.1.el6.x86_64 #1 SMP Tue Jun 11 17:23:30 CDT 2013 x86_64 x86_64 x86_64 GNU/Linux"
    '''

    glite_id = None
    ce_id    = None
    exec_beg = None
    exec_end = None
    exec_ec  = None
    wn_host  = None
    wn_syst  = None

    re_glite_id = re.compile('^(GLITE_WMS|GRID)_JOBID=(https:\/\/.*:9000\/.*)$')
    re_ce_id    = re.compile('^CE_ID=(.*)$')
    re_exec_beg = re.compile('^\-\s+Exe started at\s+:\s+(.*)$')
    re_exec_end = re.compile('^\-\s+Exe finished at\s+:\s+(.*)$')
    re_exec_ec  = re.compile('^\-\s+The exit code of the exe:\s+(\S+)$')
    re_wn_host  = re.compile('^\-\s+Ran on host.*:\s+(\S+)$')
    re_wn_syst  = re.compile('^\-\s+Host info.*\s+:\s+(.*)$')

    if os.path.exists(logw):
        f = open(logw, 'r')
 
        for l in f:
 
            ml = l.strip()

            ## found exec start time 
            m = re_exec_beg.match(ml)
            if m:
                exec_beg = m.group(1)
                continue

            ## found exec finish time 
            m = re_exec_end.match(ml)
            if m:
                exec_end = m.group(1)
                continue

            ## found exec exit code 
            m = re_exec_ec.match(ml)
            if m:
                exec_ec = m.group(1)
                continue
 
            ## found glite_id
            m = re_glite_id.match(ml)
            if m:
                glite_id = m.group(2)
                continue
 
            ## found ce_id
            m = re_ce_id.match(ml)
            if m:
                ce_id = m.group(1)
                continue

            ## found wn_host
            m = re_wn_host.match(ml)
            if m:
                wn_host = m.group(1)
                continue

            ## found wn_syst
            m = re_wn_syst.match(ml)
            if m:
                wn_syst = ' '.join( m.group(1).split()[:3] )
                continue
 
        f.close()
    else:
        logger.warning('gridnfo.log not found: %s' % logw)

    return (glite_id, ce_id, wn_host, wn_syst, exec_beg, exec_end, exec_ec)

def finder(wfs, uid=None):
    '''
    - find workspace of the workflow execution within DCI_BRIDGE_LOG_PATH.
    - extract gLite job ids belong to the workflow execution.
    - return a "guse_info" dictionary containg information of the workflow execution.
    '''

    ## output data structure
    guse_info = []
    re_pat    = []

    for wf in wfs:
        guse_info.append( {'wfname': wf,
                           'uid'   : uid,
                           'jobs'  : {}} )
 
        ## this search pattern constructed for the XML file of guse.jsdl
        if uid is None:
            re_pat.append( re.compile('^<URI>.*FileUploadServlet\?path=((.*)\/(.*)\/(%s)\/.*)<\/URI>$' % wf) )
        else:
            re_pat.append( re.compile('^<URI>.*FileUploadServlet\?path=((.*)\/(%s)\/(%s)\/.*)<\/URI>$' % (uid,wf)) )

    ## this search pattern constructed for the XML file of guse.logg
    re_submit = re.compile('^<name>logg.job.middlewareid</name>$')
    re_fail   = re.compile('^<name>job.status.failed</name>$')
    re_time   = re.compile('^<timestamp>(\d+)</timestamp>$')
    re_id     = re.compile('^<info>(https:\/\/.*:9000\/.*)</info>$')

    ## try to find workspace in DCI_BRIDGE_LOG_PATH (jobs were failed or still in running state)
    ## or in GUSE_STORAGE (jobs were finished either succeed or failed)
    for ck_dir in ck_dirs:

        logger.info('searching in %s ...' % ck_dir)

        for root,dirs,files in os.walk(ck_dir):
 
            if len( list(set(ck_files) & set(files) ) ) == len(ck_files): ## all expected files are available
 
                logger.debug('checking directory %s ...' % root)
 
                f_guse_jsdl = os.path.join(root, ck_f_guse_jsdl)
 
                f = open(f_guse_jsdl)
 
                for l in f:

                    m = None

                    for i in range(len(re_pat)):

                        m = re_pat[i].match(l.strip())
                     
                        if m:   ## match the string <uid>/<wfname> in guse.jsdl
                     
                            logger.debug('=====')
                            logger.debug('matched patterns for locating workdir ...')
                            logger.debug('%s' % f_guse_jsdl)
                            logger.debug(' |- group 1: %s' % m.group(1))
                            logger.debug(' |- group 2: %s' % m.group(2))
                            logger.debug(' |- group 3: %s' % m.group(3))
                            logger.debug(' |- group 4: %s' % m.group(4))

                            if uid is None: 
                                guse_info[i]['uid'] = m.group(3)
                     
                            ## look for glite job ids within the workdir
                            if root not in guse_info[i]['jobs'].keys():
                                guse_info[i]['jobs'][root] = {'t_beg':None, 't_end':None, 'is_failed':False, 'glite':[]}
                     
                            f_guse_logg = os.path.join(root, ck_f_guse_logg)
                            f_guse_logw = os.path.join(root, ck_f_guse_logw)
                            f_job_url   = os.path.join(root, ck_f_job_url)
                     
                            ## search for gLite job id in f_guse_logg
                            ## TODO: having impression that this log is only for the first submission, need to check it!! 
                            f1         = open(f_guse_logg)
                            ick_submit = False
                            ick_fail   = False
                            t_submit   = None
                            t_failed   = None

                            for l1 in f1:
                                ## find an entry for glite job submission
                                m1 = re_submit.match(l1.strip()) 
                                if m1:
                                    ick_submit = True
                                    continue

                                ## find an entry stating the job is failed
                                m1 = re_fail.match(l1.strip())
                                if m1:
                                    ick_fail = True
                                    guse_info[i]['jobs'][root]['is_failed'] = True
                                    continue

                                ## find the first gLite job attempt 
                                m1 = re_id.match(l1.strip())
                                if m1:
                                    glite_info = GLiteJobInfo.fromList( m1.group(1), guse_info[i]['jobs'][root]['glite'] )
                                    glite_info.setHistory('submitted', guse_info[i]['jobs'][root]['t_beg'])
                                    guse_info[i]['jobs'][root]['glite'].append(glite_info)
                                    continue
                             
                                ## find an entry of timestamp
                                m1 = re_time.match(l1.strip())
                                if m1:
                                    if ick_submit:  ## an entry about job submission time
                                        t_submit = int(m1.group(1)) / 1000
                                        guse_info[i]['jobs'][root]['t_beg'] = makeStructTimeUTC(t_submit)
                                        ick_submit = False
                                    elif ick_fail:  ## an entry about job failing time
                                        t_fail = int(m1.group(1)) / 1000
                                        guse_info[i]['jobs'][root]['t_end'] = makeStructTimeUTC(t_fail)
                                        ick_fail = False
                                    else:
                                        pass
                                    continue

                            f1.close()

                            ## parse f_guse_logg to get basic job information
                            ## TODO: having impression that this log is only for the first submission, need to check it!! 
                            #t1 = time.time()
                            #f1 = open(f_guse_logg)
                            #c1 = f1.read() + '</dcilogg>' ## the guse.logg has the closure tag missing
                            #f1.close()
                            #logger.debug('=====')
                            #logger.debug('time for reading %s: %d secs.' % (f_guse_logg, time.time()-t1))
                            #logger.debug('=====')
                            #
                            ## parse XML with ElementTree 
                            #t1 = time.time()
                            #hist_ts = {}
                            #xmlroot    = ET.fromstring(c1)
                            #glite_info = None
                            #for item in xmlroot.findall(guse_logg_xlns+'item'):
                            #    ## get item for glite id
                            #    if item.find(guse_logg_xlns+'name').text == 'logg.job.middlewareid':
                            #        glite_id = item.find(guse_logg_xlns+'info').text
                            #        logger.debug('%s' % f_guse_logg)
                            #        logger.debug(' |- gLite job id: %s' % glite_id)
                            #
                            #        glite_info = GLiteJobInfo.fromList( glite_id, guse_info[i]['glite_ids'][root] )
                            #        t_submit = int(item.find(guse_logg_xlns+'timestamp').text) / 1000.
                            #        glite_info.setHistory('submitted', t_submit )
                            #        guse_info[i]['glite_ids'][root].append(glite_info)
                            #
                            #logger.debug('=====')
                            #logger.debug('time spend parsing %s: %d secs.' % (f_guse_logg, time.time()-t1))
                            #logger.debug('=====')

                            ## get gLite job id from f_job_url (latest resubmission)
                            if os.path.exists(f_job_url):
                                f2 = open(f_job_url)
                                for l2 in f2:
                                    l2 = l2.strip()
                                    if l2 not in map(lambda x:x.id, guse_info[i]['jobs'][root]['glite']):
                                        guse_info[i]['jobs'][root]['glite'].append( GLiteJobInfo(id=l2) )
                                f2.close()

                            ## get gLite job id, worker node name, worker node os from gridnfo.log
                            (glite_id, ce_id, wn_host, wn_sys, exec_beg, exec_end, exec_ec) = find_wn_info(f_guse_logw)
                            glite_info = GLiteJobInfo.fromList( glite_id, guse_info[i]['jobs'][root]['glite']  )
                            glite_info.dest_ce     = ce_id
                            glite_info.dest_wn     = wn_host
                            glite_info.dest_wn_sys = wn_sys
                            glite_info.ec_wrapper  = exec_ec
                            glite_info.setHistory('exec_beg', exec_beg)
                            glite_info.setHistory('exec_end', exec_end)

                            if glite_id and glite_info not in guse_info[i]['jobs'][root]['glite']:
                                guse_info[i]['jobs'][root]['glite'].append( glite_info )
                            
                            break
                        else:
                            continue

                    if m:  ## this line in guse_jsdl has a match to wf/uid, the rest can be skip.
                        break
 
                f.close()

    ## call glite-wms-job-status to get job status 
    jobids      = []
    glite_jlist = []
    for wf in guse_info:
        for wdir in wf['jobs']:
            glite_jlist += wf['jobs'][wdir]['glite']
    jobids = map( lambda x:x.id, glite_jlist )

    if jobids:
        (glite_info,missing_glite_jids) = get_job_info(jobids)

        ## merge guse_info, glite_info and missing_glite_jids into one single data structure
        for jid in missing_glite_jids:
            jinfo = GLiteJobInfo.fromList( jid, glite_jlist )
            jinfo.status = 'Removed'
            jinfo.reason = 'Job removed from WMS'

        for jid in map(lambda x:x.id, glite_info):
            jinfo = GLiteJobInfo.fromList( jid, glite_jlist )
            jinfo.update( glite_info[glite_info.index(jinfo)] )

    ## resolve start and finish time of every workflow jobs
    for info in guse_info:                            ## workflows

        for dir in sorted(info['jobs'].keys()):       ## jobs of each workflow

            t_beg = info['jobs'][dir]['t_beg']
            t_end = info['jobs'][dir]['t_end']

            for jinfo in info['jobs'][dir]['glite']:  ## attempts of each job
             
                try:
                    t = jinfo.history['submitted']
                    if t_beg is None or t < t_beg: info['jobs'][dir]['t_beg'] = t
                except KeyError,e:
                    pass

                try:
                    t = jinfo.history['failed']
                    if t_end is None or t > t_end: info['jobs'][dir]['t_end'] = t
                except KeyError,e:
                    pass

                try:
                    t = jinfo.history['exec_beg']
                    if t_beg is None or t < t_beg: info['jobs'][dir]['t_beg'] = t
                except KeyError,e:
                    pass

                try:
                    t = jinfo.history['exec_end']
                    if t_end is None or t > t_end: info['jobs'][dir]['t_end'] = t
                except KeyError,e:
                    pass

                try:
                    t = jinfo.history['last_update']
                    if t_end is None or t > t_end: info['jobs'][dir]['t_end'] = t
                except KeyError,e:
                    pass

    return guse_info

## the main program
def main():

## parsing opt/args using optparse
## TODO: optparse has been deprecated in Python 2.7, migrate to argparse

    usage = '''

  %prog [options] WFNAME_1 WFNAME_2 ...

example:

  %prog [-v|--verbose] Freesurfer_V5_NSG_1792_2013-10-15-154837''' 

    parg = OptionParser(usage=usage, version="%prog 1.0")

    parg.add_option('-v','--vebose',
                    action  = 'store',
                    dest    = 'verbose',
                    choices = ['-1', '0', '1', '2'],  ## choices work only with str
                    default = '0',
                    help    = 'set one of the following verbosity levels. -1:ERROR, 0|default:WARNING, 1:INFO, 2:DEBUG')

    parg.add_option('-c', '--config',
                    action  = 'store',
                    dest    = 'fconfig',
                    default = os.path.dirname(os.path.abspath(__file__)) + '/config.ini',
                    help    = 'set the configuration parameters, see "config.ini"')

    parg.add_option('-f', '--format',
                    action = 'store',
                    dest   = 'format',
                    choices = ['plain', 'json'],
                    default = 'plain',
                    help    = 'set output format')

    parg.add_option('-p', '--provenance',
                    action = 'store_true',
                    dest   = 'provenance',
                    default = False,
                    help    = 'set to run over workflows in the provenance database and fill in job attributes')

    (options, wfs) = parg.parse_args()

    ## check if necessary argument is given
    if len(wfs) < 1 and not options.provenance:
        parg.error("at least one WFNAME must be given!")

    ## load configuration and set global variables
    c = getConfig(options.fconfig)

    ## the search dirs
    global ck_dirs
    ## the key guse files to check
    global ck_f_guse_jsdl
    global ck_f_guse_logg
    global ck_f_guse_logw
    global ck_f_job_url
    global ck_files
    ## the global logger ##
    global logger
    ## the pretty print for python dictionary ##
    global pp
    ## the provenance database connection ##
    global provdb_user
    global provdb_pass
    global provdb_name

    ## set up global logger and its logging level ##
    logger = getMyLogger(os.path.basename(__file__))

    vlv = int(options.verbose)
    if vlv < 0:
        logger.setLevel(logging.ERROR)
    elif vlv == 1:
        logger.setLevel(logging.INFO)
    elif vlv >= 2:
        logger.setLevel(logging.DEBUG)

    ## set the search dirs
    ck_dirs = map( lambda x:x.strip(), c.get('GUSE','LOG_DIRS').split(',') )

    ## set key guse files to check
    ck_f_guse_jsdl = c.get('GUSE','JSDL_FILE')
    ck_f_guse_logg = c.get('GUSE','LOGG_FILE')
    ck_f_guse_logw = c.get('GUSE','WN_LOG_FILE')
    ck_f_job_url   = c.get('GUSE','JOB_URL_FILE')
    ck_files = map( lambda x:x.strip(), c.get('GUSE','CK_FILES').split(',') )

    ## set up pretty print for python dictionary ##
    pp = pprint.PrettyPrinter(indent=4)

    ## path for robot proxy ##
    os.environ['X509_USER_PROXY'] = c.get('GRID','X509_USER_PROXY')

    ## provenance database
    provdb_user = c.get('ProvDB','USER')
    provdb_pass = c.get('ProvDB','PASS')
    provdb_name = c.get('ProvDB','DBNAME')

    ## run the finder followed by actions on the retrieved job attributes
    prov_wfs = {}
    if options.provenance:
        prov_wfs = qryWorkflowToProcessFromProvDB()
        updateProvDB( prov_wfs, finder(prov_wfs.values()) )
    else:
        report( finder(wfs), format=options.format )

## execute the main program
if __name__ == "__main__":
    main()
