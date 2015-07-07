// ----------------------------------------------------------------------------
// Copyright 2007-2015, GeoTelematic Solutions, Inc.
// All rights reserved
// ----------------------------------------------------------------------------
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
// http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
// ----------------------------------------------------------------------------
// Change History:
//  2015/05/03  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.dbtypes;

import java.lang.*;
import java.util.*;
import java.math.*;
import java.io.*;
import java.sql.*;

import org.opengts.util.*;
import org.opengts.dbtools.*;
import org.opengts.db.StatusCodes;
import org.opengts.db.tables.EventData;
import org.opengts.db.tables.Driver; // DutyStatus

public class DTELogState
    extends DBFieldType
{

    // ------------------------------------------------------------------------

    public  static final String  KEY_enabled            = "enabled";
    public  static final String  KEY_lastTimestamp      = "lastTS";
    public  static final String  KEY_lastStatusCode     = "lastSC";
    public  static final String  KEY_isDriving          = "isDriving";
    public  static final String  KEY_distanceKM         = "distKM";

    public  static final boolean DFT_enabled            = false;
    public  static final long    DFT_lastTimestamp      = 0L;
    public  static final int     DFT_lastStatusCode     = StatusCodes.STATUS_NONE;
    public  static final boolean DFT_isDriving          = false;
    public  static final double  DFT_distanceKM         = 0.0;

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    private boolean enabled         = DFT_enabled;
    private long    lastTimestamp   = DFT_lastTimestamp;
    private int     lastStatusCode  = DFT_lastStatusCode;
    private boolean isDriving       = DFT_isDriving;
    private double  lastDistanceKM  = DFT_distanceKM;

    /**
    *** Default constructor (disabled)
    **/
    public DTELogState()
    {
        this.init(null);
    }

    /**
    *** Constructor
    **/
    public DTELogState(RTProperties rtp)
    {
        this.init(rtp);
    }

    /**
    *** Constructor
    **/
    public DTELogState(String rtpStr)
    {
        this.init(new RTProperties(rtpStr));
    }            

    /**
    *** Constructor
    **/
    public DTELogState(boolean enabled, long lastTS, int lastSC, boolean isDriving, double distKM)
    {
        this.init(enabled, lastTS, lastSC, isDriving, distKM);
    }

    /** [DBFieldType interface]
    *** Constructor
    **/
    public DTELogState(ResultSet rs, String fldName)
        throws SQLException
    {
        super(rs, fldName);
        String elp = (rs != null)? rs.getString(fldName) : null;
        if (!StringTools.isBlank(elp)) {
            this.init(new RTProperties(elp));
        } else {
            this.init(null);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Initializes values
    **/
    private void init(boolean enabled, long lastTS, int lastSC, boolean isDriving, double distKM) 
    {
        this.setEnabled(enabled);
        this.setLastTimestamp(lastTS);
        this.setLastStatusCode(lastSC);
        this.setIsDriving(isDriving);
        this.setLastDistanceKM(distKM);
    }

    /**
    *** Initializes values
    **/
    public void init(RTProperties rtp) 
    {
        if (rtp != null) {
            this.init(
                rtp.getBoolean(KEY_enabled       , DFT_enabled),
                rtp.getLong(   KEY_lastTimestamp , DFT_lastTimestamp),
                rtp.getInt(    KEY_lastStatusCode, DFT_lastStatusCode),
                rtp.getBoolean(KEY_isDriving     , DFT_isDriving),
                rtp.getDouble( KEY_distanceKM    , DFT_distanceKM)
                );
        } else {
            this.clear();
        }
    }

    /**
    *** Clears all values to their default
    **/
    public void clear()
    {
        this.init(
            DFT_enabled,
            DFT_lastTimestamp,
            DFT_lastStatusCode,
            DFT_isDriving,
            DFT_distanceKM
            );
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the enabled state 
    **/
    public boolean getEnabled()
    {
        return this.enabled;
    }

    /**
    *** Sets the enabled state 
    **/
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the last timestamp 
    **/
    public long getLastTimestamp()
    {
        return this.lastTimestamp;
    }

    /**
    *** Sets the last timestamp 
    **/
    public void setLastTimestamp(long lastTS)
    {
        this.lastTimestamp = lastTS;
    }

    /**
    *** Returns true if last timestamp is defined
    **/
    public boolean hasLastTimestamp()
    {
        return (this.getLastTimestamp() > 0L)? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the last status-code 
    **/
    public int getLastStatusCode()
    {
        return this.lastStatusCode;
    }

    /**
    *** Sets the last status-code 
    **/
    public void setLastStatusCode(int lastSC)
    {
        this.lastStatusCode = lastSC;
    }

    // ------------------------------------------------------------------------

    /**
    *** Set Last timestamp and status-code per specified EventData instance
    **/
    public void setLastEventData(EventData ev)
    {
        if (ev != null) {
            this.setLastTimestamp(ev.getTimestamp());
            this.setLastStatusCode(ev.getStatusCode());
        }
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the driving state 
    **/
    public boolean getIsDriving()
    {
        return this.isDriving;
    }

    /**
    *** Sets the driving state 
    **/
    public void setIsDriving(boolean isDriving)
    {
        this.isDriving = isDriving;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the distance travelled, in Kilometers
    **/
    public double getLastDistanceKM()
    {
        return this.lastDistanceKM;
    }

    /**
    *** Sets the distance travelled, in Kilometers
    **/
    public void setLastDistanceKM(double distKM)
    {
        this.lastDistanceKM = distKM;
    }

    /**
    *** Adds the specified distance to the accumulator, in Kilometers
    **/
    public void addLastDistanceKM(double deltaKM)
    {
        this.lastDistanceKM += deltaKM;
    }

    // --------------------------------

    /**
    *** Gets the distance travelled, in Miles
    **/
    public double getLastDistanceMI()
    {
        return this.getLastDistanceKM() * GeoPoint.MILES_PER_KILOMETER;
    }

    /**
    *** Sets the distance travelled, in Miles
    **/
    public void setLastDistanceMI(double distMI)
    {
        this.setLastDistanceKM(distMI * GeoPoint.KILOMETERS_PER_MILE);
    }

    /**
    *** Adds the specified distance to the accumulator, in Miles
    **/
    public void addLastDistanceMI(double deltaMI)
    {
        this.addLastDistanceKM(deltaMI * GeoPoint.KILOMETERS_PER_MILE);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Gets the RTProperties representation of this instance
    **/
    public RTProperties getRTProperties()
    {
        RTProperties rtp = new RTProperties();
        rtp.setBoolean(KEY_enabled       , this.getEnabled());
        rtp.setLong(   KEY_lastTimestamp , this.getLastTimestamp());
        rtp.setInt(    KEY_lastStatusCode, this.getLastStatusCode());
        rtp.setBoolean(KEY_isDriving     , this.getIsDriving());
        rtp.setDouble( KEY_distanceKM    , this.getLastDistanceKM());
        return rtp;
    }

    /**
    *** Gets the String representation of this instance
    **/
    public String toString()
    {
        return this.getRTProperties().toString();
    }

    // ------------------------------------------------------------------------

    /** [DBFieldType interface]
    *** Return Object as saved in the DB
    **/
    public Object getObject()
    {
        return this.toString();
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if the specified Object is equal to this instance
    **/
    public boolean equals(Object other)
    {
        if (other instanceof DTELogState) {
            DTELogState es = (DTELogState)other;
            return this.toString().equals(es.toString());
        } else {
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    public static void main(String argv[])
    {
        RTConfig.setCommandLineArgs(argv);
    }

}
