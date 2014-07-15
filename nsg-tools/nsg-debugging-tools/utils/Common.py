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
 

#/usr/bin/env python

import os 
import logging
import inspect
import time
import locale
import ConfigParser

try:
    ## set up for MySQLdb (assuming that this script will be executed with guse account)
    os.environ['PYTHON_EGG_CACHE'] = '/home/guse/.python-eggs'
    import MySQLdb as mdb
except Exception,e:
    import mysql.connector as mdb
    from mysql.connector import errorcode

def getMyLogger(name=None):

    if name is None:
        name = inspect.stack()[1][3]

    logging.basicConfig(format='[%(levelname)s:%(name)s] %(message)s', level=logging.ERROR)
    return logging.getLogger(name)

def getConfig(config_file='config.ini'):
    '''
    read and parse the config.ini file
    '''

    default_cfg = {
        'LOG_DIR_BASE'   : '/home/guse/guse/apache-tomcat-6.0.35/temp',
        'JSDL_FILE'      : 'guse.jsdl',
        'LOGG_FILE'      : 'guse.logg',
        'WN_LOG_FILE'    : 'gridnfo.log',
        'JOB_URL_FILE'   : 'job.url',
        'X509_USER_PROXY': '/home/guse/robot/robot_proxy'
    }

    config = ConfigParser.SafeConfigParser(default_cfg)
    config.read(config_file)

    return config


def parseTimeStringLocale(timeString):
    '''
    parse locale-dependent time string with timezone into seconds since epoch 
    '''

    t = None

    logger = getMyLogger()

    ## try parsing the time string with different locale
    for l in ['en_US', 'nl_NL']:
        locale.setlocale(locale.LC_TIME, l)
        try:
            t = time.strptime(timeString, '%a %b %d %H:%M:%S %Y %Z')
        except ValueError,e:
            try:
                t = time.strptime(timeString, '%a %b %d %H:%M:%S %Z %Y')
            except ValueError,e:
                pass
        if t:
            break

    ## set back to default locale
    locale.setlocale(locale.LC_TIME, '')

    return time.mktime(t)

def makeStructTimeUTC(value):
    '''
    Convert given time value into proper timestamp in UTC
    '''
    utc_tt = None
    if type(value) in [int,float,long]: ## value is second from epoch
        utc_tt = time.gmtime(value)
    elif type(value) == str: ## value is a string with timezone
        utc_tt = time.gmtime( parseTimeStringLocale(value) )
        if not utc_tt:
            raise ValueError('cannot parse time string: %s' % value)
    elif value == None:
        pass
    else:
        utc_tt = value

    return utc_tt

def fmtStructTimeUTC(stime):
    '''
    Format given struct time in to human readable string
    ''' 

    logger = getMyLogger()

    try:
        return time.strftime('%a %b %d %H:%M:%S %Y',stime)
    except TypeError,e:
        logger.error('unable to format time string: %s' % repr(stime))
        return None

def getMySQLConnector(uid,passwd,db):

    logger = getMyLogger()
    cnx    = None
    config = None 

    if mdb.__name__ == 'MySQLdb':
        ### use MySQLdb library
        config = {'user'   : uid,
                  'passwd' : passwd,
                  'db'     : db,
                  'host'   : 'localhost'}
        try:
            cnx = mdb.connect(**config)
        except mdb.Error, e:
            logger.error('db query error %d: %s' % (e.args[0],e.args[1]))

            if cnx: cnx.close()
    else:
        ### use mysql-connector library
        config = {'user'             : uid,
                  'password'         : passwd,
                  'database'         : db,
                  'host'             : 'localhost',
                  'raise_on_warnings': True }
        try:
            cnx = mdb.connect(**config)
        except mdb.Error, err:
            if err.errno == errorcode.ER_ACCESS_DENIED_ERROR:
                logger.error("something is wrong with your user name or password")
            elif err.errno == errorcode.ER_BAD_DB_ERROR:
                logger.error("database does not exists")
            else:
                logger.error(err)

            if cnx: cnx.close()

    return cnx
