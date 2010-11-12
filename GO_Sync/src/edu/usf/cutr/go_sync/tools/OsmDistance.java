/**
Copyright 2010 University of South Florida

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

**/
package edu.usf.cutr.go_sync.tools;

public class OsmDistance {
    /**
 * Calculates geodetic distance between two points specified by latitude/longitude using
 * Vincenty inverse formula for ellipsoids
 *
 * @param   {Number} lat1, lon1: first point in decimal degrees
 * @param   {Number} lat2, lon2: second point in decimal degrees
 * @returns (Number} distance in metres between points
 */
    public static double distVincenty(String latitude1, String longitude1, String latitude2, String longitude2) {
        double lat1 = Double.parseDouble(latitude1);
        double lon1 = Double.parseDouble(longitude1);
        double lat2 = Double.parseDouble(latitude2);
        double lon2 = Double.parseDouble(longitude2);
        double a = 6378137;
        double b = 6356752.3142;
        double f = 1/298.257223563;  // WGS-84 ellipsoid params
        double  L = Math.toRadians(lon2-lon1);
        double U1 = Math.atan((1-f) * Math.tan(Math.toRadians(lat1)));
        double U2 = Math.atan((1-f) * Math.tan(Math.toRadians(lat2)));
        double sinU1 = Math.sin(U1);
        double cosU1 = Math.cos(U1);
        double sinU2 = Math.sin(U2);
        double cosU2 = Math.cos(U2);

        double lambda = L;
        double lambdaP;
        double iterLimit = 100;

        double cosSqAlpha, sinSigma, cos2SigmaM, sinAlpha, sigma, cosSigma;
        do {
            double sinLambda = Math.sin(lambda);
            double cosLambda = Math.cos(lambda);
            sinSigma = Math.sqrt((cosU2*sinLambda) * (cosU2*sinLambda) +
                    (cosU1*sinU2-sinU1*cosU2*cosLambda) * (cosU1*sinU2-sinU1*cosU2*cosLambda));

            if (sinSigma==0) return 0;  // co-incident points

            cosSigma = sinU1*sinU2 + cosU1*cosU2*cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            sinAlpha = cosU1 * cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1 - sinAlpha*sinAlpha;
            cos2SigmaM = cosSigma - 2*sinU1*sinU2/cosSqAlpha;
            if (Double.isNaN(cos2SigmaM)) cos2SigmaM = 0;  // equatorial line: cosSqAlpha=0 (§6)
            double C = f/16*cosSqAlpha*(4+f*(4-3*cosSqAlpha));

            lambdaP = lambda;
            lambda = L + (1-C) * f * sinAlpha * (sigma + C*sinSigma*(cos2SigmaM+C*cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)));
        } while (Math.abs(lambda-lambdaP) > 1e-12 && --iterLimit>0);

        if (iterLimit==0) return Double.NaN;  // formula failed to converge

        double uSq = cosSqAlpha * (a*a - b*b) / (b*b);
        double A = 1 + uSq/16384*(4096+uSq*(-768+uSq*(320-175*uSq)));
        double B = uSq/1024 * (256+uSq*(-128+uSq*(74-47*uSq)));
        double deltaSigma = B*sinSigma*(cos2SigmaM+B/4*(cosSigma*(-1+2*cos2SigmaM*cos2SigmaM)-
                B/6*cos2SigmaM*(-3+4*sinSigma*sinSigma)*(-3+4*cos2SigmaM*cos2SigmaM)));
        double s = b*A*(sigma-deltaSigma);

//        int temp = (int)(s * 1000);
//        s = (double)(temp/1000.0); // round to 1mm precision

        return s;
    }
}