/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.sdmx;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpStatus;
import org.geotools.data.Query;
import org.geotools.data.sdmx.SDMXFeatureReader;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.util.logging.Logging;
import org.geotools.filter.text.cql2.CQL;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Expression;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import it.bancaditalia.oss.sdmx.client.RestSdmxClient;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ RestSdmxClient.class, HttpURLConnection.class, URL.class })
public class SDMXFeatureReaderTest {

  private static final Logger LOGGER = Logging
      .getLogger("org.geotools.data.arcgisrest");

  private SDMXDataStore dataStore;
  private URL urlMock;
  private HttpURLConnection clientMock;

  SDMXFeatureReader reader;
  SDMXFeatureSource source;
  SimpleFeatureType fType;

  @Test
  public void noFeaturesNext() throws Exception {

    this.urlMock = PowerMockito.mock(URL.class);
    this.clientMock = PowerMockito.mock(HttpURLConnection.class);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(this.urlMock);
    PowerMockito.when(this.urlMock.openConnection())
        .thenReturn(this.clientMock);
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK);
    when(clientMock.getInputStream())
        .thenReturn(Helper.readXMLAsStream("test-data/abs.xml"))
        .thenReturn(
            Helper.readXMLAsStream("test-data/abs-census2011-t04-abs.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/abs-seifa-lga.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/query-t04.xml"));

    this.dataStore = (SDMXDataStore) Helper.createDefaultSDMXTestDataStore();
    this.fType = this.dataStore.getFeatureSource(Helper.T04).getSchema();
    this.source = (SDMXFeatureSource) this.dataStore
        .getFeatureSource(Helper.T04);

    this.source.buildFeatureType();
    this.reader = (SDMXFeatureReader) this.source.getReader(Query.ALL);

    assertTrue(this.reader.hasNext());
    SimpleFeature feat;
    while (this.reader.hasNext()) {
      feat = this.reader.next();
      String s = feat.getID() + ": ";
      for (int i = 0; i < feat.getAttributeCount(); i++) {
        s += "|" + feat.getAttribute(i);
        // assertNull(feat.getAttribute(0));
      }
      System.out.println(s);
    }
  }

  @Test
  public void noFeatures() throws Exception {

    this.urlMock = PowerMockito.mock(URL.class);
    this.clientMock = PowerMockito.mock(HttpURLConnection.class);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(this.urlMock);
    PowerMockito.when(this.urlMock.openConnection())
        .thenReturn(this.clientMock);
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_NOT_FOUND);
    when(clientMock.getInputStream())
        .thenReturn(Helper.readXMLAsStream("test-data/abs.xml"))
        .thenReturn(
            Helper.readXMLAsStream("test-data/abs-census2011-t04-abs.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/abs-seifa-lga.xml"))
        .thenReturn(new ByteArrayInputStream("".getBytes()));

    this.dataStore = (SDMXDataStore) Helper.createDefaultSDMXTestDataStore();
    this.fType = this.dataStore.getFeatureSource(Helper.T04).getSchema();
    this.source = (SDMXFeatureSource) this.dataStore
        .getFeatureSource(Helper.T04);

    this.source.buildFeatureType();
    this.reader = (SDMXFeatureReader) this.source.getReader(Query.ALL);

    assertFalse(this.reader.hasNext());
    assertNull(this.reader.next());
  }

  @Test
  public void queryExpression() throws Exception {

    this.urlMock = PowerMockito.mock(URL.class);
    this.clientMock = PowerMockito.mock(HttpURLConnection.class);

    PowerMockito.whenNew(URL.class).withAnyArguments().thenReturn(this.urlMock);
    PowerMockito.when(this.urlMock.openConnection())
        .thenReturn(this.clientMock);
    when(clientMock.getResponseCode()).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK).thenReturn(HttpStatus.SC_OK)
        .thenReturn(HttpStatus.SC_OK);
    when(clientMock.getInputStream())
        .thenReturn(Helper.readXMLAsStream("test-data/abs.xml"))
        .thenReturn(
            Helper.readXMLAsStream("test-data/abs-census2011-t04-abs.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/abs-seifa-lga.xml"))
        .thenReturn(Helper.readXMLAsStream("test-data/query-t04.xml"));

    this.dataStore = (SDMXDataStore) Helper.createDefaultSDMXTestDataStore();
    this.fType = this.dataStore.getFeatureSource(Helper.T04).getSchema();
    this.source = (SDMXFeatureSource) this.dataStore
        .getFeatureSource(Helper.T04);

    assertEquals("*.*.*.*.*.*.*", this.source.buildConstraints(Query.ALL));

    // url -XGET
    // "http://stat.data.abs.gov.au/restsdmx/sdmx.ashx/GetData/ABS_CENSUS2011_T04/3.TOT.TOT.0.AUS.0.A/ABS"
    Filter filter = CQL.toFilter("MEASURE='3' and "
        + "MSTP='TOT' and "
        + "AGE='TOT' and "
        + "STATE='0' and "
        + "REGIONTYPE='AUS' and "
        + "REGION='0' and "
        + "FREQUENCY='A'");
    assertEquals("3.TOT.TOT.0.AUS.0.A",
        this.source.buildConstraints(new Query("", filter)));
  }

}
