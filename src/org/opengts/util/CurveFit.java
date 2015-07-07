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
// Description:
//  Partial implementation of a ClientPacketHandler
// ----------------------------------------------------------------------------
// Change History:
//  2014/10/22  Martin D. Flynn
//     -Initial release
// ----------------------------------------------------------------------------
package org.opengts.util;

import java.lang.*;
import java.util.*;

/**
*** Curve-fit profile matrix
**/
public class CurveFit
{

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Parse XY pair list.
    *** Leading/Trailing list characters (..) or [..], are optional.
    *** Does not reutrn null.
    **/
    public static XYPair[] ParseXYPair(String xyListStr)
    {
        return XYPair.ParseXYPair(xyListStr);
    }

    /**
    *** Parse XY pair list.
    *** Leading/Trailing list characters (..) or [..], are required.
    *** Does not reutrn null.
    **/
    public static XYPair[] ParseXYPair(String xyListStr, int fromNdx)
    {
        return XYPair.ParseXYPair(xyListStr, fromNdx);
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
    *** Calculates the polynomial coefficients
    **/
    protected static double[] _CalculateCoefficients(double M[][], double V[])
    {

        /* validate */
        if ((M == null) || (V == null) || (M.length < 2) || (M.length != V.length)) {
            return null;
        }

        /* Gaussian elimination */
        // -- algorithm obtained by comparing several web-based references
        int L = M.length; // length (>= 2)
        for (int pr = 0; pr < L; pr++) {
            // -- find/swap largest remaining row
            int mx = pr;
            for (int i = pr + 1; i < L; i++) {
                if (Math.abs(M[i][pr]) > Math.abs(M[mx][pr])) {
                    mx = i;
                }
            }
            ListTools.swap(M, pr, mx);
            ListTools.swap(V, pr, mx);
            // -- pivot
            double P = M[pr][pr]; // largest abs value
            if (P == 0.0) {
                // -- unable to complete (divide by zero)
                Print.logWarn("Unable to calculate curve-fit coefficients");
                return null;
            }
            for (int i = pr + 1; i < L; i++) {
                double c = M[i][pr] / P;
                for (int j = pr; j < L; j++) {
                    M[i][j] -= c * M[pr][j];
                }
                V[i] -= c * V[pr];
            }
        }

        /* calculate coefficients */
        double C[] = new double[L];
        for (int i = L - 1; i >= 0; i--) {
            double S = 0.0;
            for (int j = i + 1; j < L; j++) { // <-- skipped on first pass
                S += M[i][j] * C[j];
            }
            C[i] = (V[i] - S) / M[i][i];
        }

        /* return coefficients */
        return C;

    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    protected XYPair  minXY     = null;
    protected XYPair  maxXY     = null;
    protected double  coeff[]   = null;

    /**
    *** Protected constructor.
    *** Only allowed for subclasses
    **/
    protected CurveFit()
    {
        super();
    }

    /**
    *** Clone constructor (deep copy) 
    **/
    public CurveFit(CurveFit cf) 
    {
        this();
        if (cf != null) {
            this.minXY = (cf.minXY != null)? new XYPair(cf.minXY) : null;
            this.maxXY = (cf.maxXY != null)? new XYPair(cf.maxXY) : null;
            this.coeff = ListTools.toArray(cf.coeff,0,-1);
        }
    }

    /**
    *** XYPair constructor 
    **/
    public CurveFit(XYPair xy[])
    {
        this();
        // -- initial validation (handles xy == null)
        int len = ListTools.size(xy);
        if (len < 2) { // must have at least 2 points
            this.coeff = null;
            return;
        }
        // -- load matrix
        double M[][] = new double[len][len];
        double V[]   = new double[len];
        XYPair minP  = null;
        XYPair maxP  = null;
        for (int p = 0; p < len; p++) {
            // -- load matrix row
            double X = xy[p].getX();
            double Y = xy[p].getY();
            for (int i = 0; i < len; i++) {
                M[p][i] = Math.pow(X, (double)(len - i - 1));
            }
            V[p] = Y;
            // -- X range
            if ((minP == null) || (X < minP.getX())) { minP = xy[p]; }
            if ((maxP == null) || (X > maxP.getX())) { maxP = xy[p]; }
        }
        // -- save min/max
        this.minXY = new XYPair(minP);
        this.maxXY = new XYPair(maxP);
        // -- calculate CurveFit coefficients
        this.coeff = _CalculateCoefficients(M, V); // may return null
    }

    /**
    *** String constructor 
    **/
    public CurveFit(String xyListStr)
    {
        this(ParseXYPair(xyListStr));
    }

    /**
    *** X/Y pair constructor 
    **/
    public CurveFit(double xy[][]) {
        this();
        // -- initial validation (handles xy == null)
        int len = ListTools.size(xy);
        if (len < 2) { // must have at least 2 points
            this.coeff = null;
            return;
        }
        // -- load matrix
        double M[][]  = new double[len][len];
        double V[]    = new double[len];
        double minP[] = null;
        double maxP[] = null;
        for (int p = 0; p < len; p++) {
            // -- validate X/Y
            if (ListTools.size(xy[p]) < 2) {
                this.coeff = null;
                return;
            }
            // -- load matrix row
            double X = xy[p][0];
            double Y = xy[p][1];
            for (int i = 0; i < len; i++) {
                M[p][i] = Math.pow(X, (double)(len - i - 1));
            }
            V[p] = Y;
            // -- X range
            if ((minP == null) || (X < minP[0])) { minP = xy[p]; }
            if ((maxP == null) || (X > maxP[0])) { maxP = xy[p]; }
        }
        // -- save min/max
        this.minXY = new XYPair(minP[0],minP[1]);
        this.maxXY = new XYPair(maxP[0],maxP[1]);
        // -- calculate CurveFit coefficients
        this.coeff = _CalculateCoefficients(M, V); // may return null
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns true if this CurveFit is valid
    **/
    public boolean isValid() 
    {
        return ((this.coeff != null) && (this.coeff.length > 0))? true : false;
    }

    /**
    *** Returns true if the specified CurveFit is valid
    **/
    public static boolean isValid(CurveFit cf)
    {
        return ((cf != null) && cf.isValid())? true : false;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the size of this CurveFit
    **/
    public int size() 
    {
        return (this.coeff != null)? this.coeff.length : 0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Gets the coefficient for the specified index
    **/
    public double getCoefficient(int n)
    {
        return ((n >= 0) && (n < this.size()))? this.coeff[n] : 0.0;
    }

    // ------------------------------------------------------------------------

    /**
    *** Returns the value of Y based on the specified value for X.
    *** This function is valid only over the range specified by the originally 
    *** specified set of points.
    **/
    public double FTN(double X)
    {

        /* get/check size */
        int L = this.size(); // may be '0' if invalid
        if (L <= 0) {
            return 0.0;
        }

        /* check X range */
        if (X <= this.minXY.getX()) {
            return this.minXY.getY();
        } else
        if (X >= this.maxXY.getX()) {
            return this.maxXY.getY();
        }

        /* calculate/return Y */
        double Y = 0.0;
        for (int i = 0; i < L; i++) {
            Y += this.coeff[i] * Math.pow(X,(double)(L-i-1));
        }
        return Y;

    }

    // ------------------------------------------------------------------------

    /**
    *** Gets a String representation of this instance
    **/
    public String toString()
    {
        return this.toString(new StringBuffer(),null).toString();
    }

    /**
    *** Gets a String representation of this instance
    **/
    public StringBuffer toString(StringBuffer sb, String fmt)
    {
        if (sb == null) { sb = new StringBuffer(); }
        if (this.isValid()) {
            if (this.minXY != null) {
                sb.append("min=");
                sb.append(this.minXY.toString());
                sb.append(" ");
            }
            if (this.maxXY != null) {
                sb.append("max=");
                sb.append(this.maxXY.toString());
                sb.append(" ");
            }
            if (this.coeff != null) {
                sb.append("coeff=");
                for (int c = 0; c < this.coeff.length; c++) {
                    if (c > 0) { sb.append(","); }
                    if (!StringTools.isBlank(fmt)) {
                        sb.append(StringTools.format(this.coeff[c],fmt));
                    } else {
                        sb.append(this.coeff[c]);
                    }
                }
            }
        } else {
            sb.append("invalid");
        }
        return sb;
    }

    // ------------------------------------------------------------------------

    /**
    *** (Debug) Prints the matrix, values, and coefficients (for debug purposes only)
    **/
    public void print(String msg, double M[][], double V[])
    {
        Print.sysPrintln(msg + ":");
        if (M != null) {
            Print.sysPrintln("Matrix:");
            for (int m = 0; m < M.length; m++) {
                String fmt = "0.00000";
                StringBuffer sb = new StringBuffer();
                sb.append("| ");
                for (int i = 0; i < M[m].length; i++) {
                    sb.append(StringTools.format(M[m][i],fmt,fmt.length()+3));
                    sb.append(" ");
                }
                sb.append("|");
                // --
                sb.append("   ");
                // --
                sb.append("| ");
                sb.append(StringTools.format(V[m],fmt));
                sb.append(" |");
                Print.sysPrintln(sb.toString());
            }
        }
        // -- Coefficients
        if (this.coeff != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < ListTools.size(this.coeff); i++) {
                sb.append(this.coeff[i]);
                sb.append(",  ");
            }
            Print.sysPrintln("Coefficients: " + sb);
        }
    }

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

}
