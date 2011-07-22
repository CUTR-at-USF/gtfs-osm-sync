/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package edu.usf.cutr.go_sync.object;

/**
 *
 * @author megordon
 */
public class DefaultOperator {
    private String operatorName;
    private String operatorAbbreviation;
    private int ntdID;
    private String gtfsURL;
    private int stopDigits;

    public DefaultOperator(String operatorName, String operatorAbbreviation, int ntdID, String gtfsURL, int stopDigits) {
        this.operatorName = operatorName;
        this.operatorAbbreviation = operatorAbbreviation;
        this.ntdID = ntdID;
        this.gtfsURL = gtfsURL;
        this.stopDigits = stopDigits;
    }

    public DefaultOperator(String operatorName, String operatorAbbreviation, int ntdID, String gtfsURL) {
        this.operatorName = operatorName;
        this.operatorAbbreviation = operatorAbbreviation;
        this.ntdID = ntdID;
        this.gtfsURL = gtfsURL;
    }

    public DefaultOperator(String operatorName, String operatorAbbreviation, int ntdID) {
        this.operatorName = operatorName;
        this.operatorAbbreviation = operatorAbbreviation;
        this.ntdID = ntdID;
    }

    public DefaultOperator(String operatorName, String operatorAbbreviation) {
        this.operatorName = operatorName;
        this.operatorAbbreviation = operatorAbbreviation;
    }

    public DefaultOperator(String operatorName) {
        this.operatorName = operatorName;
    }
    
    

    /**
     * @return the operatorName
     */
    public String getOperatorName() {
        return operatorName;
    }

    /**
     * @return the operatorAbbreviation
     */
    public String getOperatorAbbreviation() {
        return operatorAbbreviation;
    }

    /**
     * @return the ntdID
     */
    public int getNtdID() {
        return ntdID;
    }

    /**
     * @return the gtfsURL
     */
    public String getGtfsURL() {
        return gtfsURL;
    }

    /**
     * @return the stopDigits
     */
    public int getStopDigits() {
        return stopDigits;
    }

    /**
     * @param operatorName the operatorName to set
     */
    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    /**
     * @param operatorAbbreviation the operatorAbbreviation to set
     */
    public void setOperatorAbbreviation(String operatorAbbreviation) {
        this.operatorAbbreviation = operatorAbbreviation;
    }

    /**
     * @param ntdID the ntdID to set
     */
    public void setNtdID(int ntdID) {
        this.ntdID = ntdID;
    }

    /**
     * @param gtfsURL the gtfsURL to set
     */
    public void setGtfsURL(String gtfsURL) {
        this.gtfsURL = gtfsURL;
    }

    /**
     * @param stopDigits the stopDigits to set
     */
    public void setStopDigits(int stopDigits) {
        this.stopDigits = stopDigits;
    }

    @Override
    public String toString() {
        return operatorName;
    }
    

}
