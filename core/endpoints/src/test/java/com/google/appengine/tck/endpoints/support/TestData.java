/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.endpoints.support;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Gregor Sfiligoj
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class TestData implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stringData;
    private Double doubledata;
    private Long longData;
    private Float floatData;
    private Integer integerData;
    private Boolean booleanData;
    private Date dateData;

    public TestData() {
    }

    public TestData(String stringData, Double doubleData, Long longData,
                    Float floatData, Integer integerData, Boolean booleanData,
                    Date dateData) {
        this.stringData = stringData;
        this.doubledata = doubleData;
        this.longData = longData;
        this.floatData = floatData;
        this.integerData = integerData;
        this.booleanData = booleanData;
        this.dateData = dateData;
    }

    public String getStringData() {
        return stringData;
    }

    public void setStringData(String stringData) {
        this.stringData = stringData;
    }

    public Double getDoubledata() {
        return doubledata;
    }

    public void setDoubledata(Double doubledata) {
        this.doubledata = doubledata;
    }

    public Long getLongData() {
        return longData;
    }

    public void setLongData(Long longData) {
        this.longData = longData;
    }

    public Float getFloatData() {
        return floatData;
    }

    public void setFloatData(Float floatData) {
        this.floatData = floatData;
    }

    public Integer getIntegerData() {
        return integerData;
    }

    public void setIntegerData(Integer integerData) {
        this.integerData = integerData;
    }

    public Boolean getBooleanData() {
        return booleanData;
    }

    public void setBooleanData(Boolean booleanData) {
        this.booleanData = booleanData;
    }

    public Date getDateData() {
        return dateData;
    }

    public void setDateData(Date dateData) {
        this.dateData = dateData;
    }

}
