/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.core.batch.v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class Header implements Cloneable {

  private final Map<String, HeaderField> headers = new HashMap<String, HeaderField>();
  private int lineNumber;

  public Header(final int lineNumer) {
    lineNumber = lineNumer;
  }

  public void addHeader(final String name, final String value, final int lineNumber) {
    final HeaderField headerField = getHeaderFieldOrDefault(name, lineNumber);
    final List<String> headerValues = headerField.getValues();

    if (!headerValues.contains(value)) {
      headerValues.add(value);
    }
  }

  public void addHeader(final String name, final List<String> values, final int lineNumber) {
    final HeaderField headerField = getHeaderFieldOrDefault(name, lineNumber);
    final List<String> headerValues = headerField.getValues();

    for (final String value : values) {
      if (!headerValues.contains(value)) {
        headerValues.add(value);
      }
    }
  }

  public boolean isHeaderMatching(final String name, final Pattern pattern) {
    if (getHeaders(name).size() != 1) {
      return false;
    } else {
      return pattern.matcher(getHeaders(name).get(0)).matches();
    }
  }

  public void removeHeader(final String name) {
    headers.remove(name.toLowerCase(Locale.ENGLISH));
  }

  public String getHeader(final String name) {
    final HeaderField headerField = getHeaderField(name);

    return (headerField == null) ? null : headerField.getValue();
  }

  public String getHeaderNotNull(final String name) {
    final HeaderField headerField = getHeaderField(name);

    return (headerField == null) ? "" : headerField.getValueNotNull();
  }

  public List<String> getHeaders(final String name) {
    final HeaderField headerField = getHeaderField(name);

    return (headerField == null) ? new ArrayList<String>() : headerField.getValues();
  }

  public HeaderField getHeaderField(final String name) {
    return headers.get(name.toLowerCase(Locale.ENGLISH));
  }

  public int getLineNumber() {
    return lineNumber;
  }

  public Map<String, String> toSingleMap() {
    final Map<String, String> singleMap = new HashMap<String, String>();

    for (Entry<String, HeaderField> header : headers.entrySet()) {
      HeaderField field = header.getValue();
      singleMap.put(field.getFieldName(), getHeader(header.getKey()));
    }

    return singleMap;
  }

  public Map<String, List<String>> toMultiMap() {
    final Map<String, List<String>> singleMap = new HashMap<String, List<String>>();

    for (Entry<String, HeaderField> header : headers.entrySet()) {
      HeaderField field = header.getValue();
      singleMap.put(field.getFieldName(), field.getValues());
    }

    return singleMap;
  }

  private HeaderField getHeaderFieldOrDefault(final String name, final int lineNumber) {
    HeaderField headerField = headers.get(name.toLowerCase(Locale.ENGLISH));

    if (headerField == null) {
      headerField = new HeaderField(name, lineNumber);
      headers.put(name.toLowerCase(Locale.ENGLISH), headerField);
    }

    return headerField;
  }

  @Override
  public Header clone() {
    final Header newInstance = new Header(lineNumber);

    for (Entry<String, HeaderField> header : headers.entrySet()) {
      newInstance.headers.put(header.getKey(), header.getValue().clone());
    }

    return newInstance;
  }

  public static List<String> splitValuesByComma(final String headerValue) {
    final List<String> singleValues = new ArrayList<String>();

    String[] parts = headerValue.split(",");
    for (final String value : parts) {
      singleValues.add(value.trim());
    }

    return singleValues;
  }

  public static class HeaderField implements Cloneable {
    private final String fieldName;
    private final List<String> values;
    private final int lineNumber;

    public HeaderField(final String fieldName, final int lineNumber) {
      this(fieldName, new ArrayList<String>(), lineNumber);
    }

    public HeaderField(final String fieldName, final List<String> values, final int lineNumber) {
      this.fieldName = fieldName;
      this.values = values;
      this.lineNumber = lineNumber;
    }

    public String getFieldName() {
      return fieldName;
    }

    public List<String> getValues() {
      return values;
    }

    public String getValue() {
      final StringBuilder result = new StringBuilder();

      for (final String value : values) {
        result.append(value);
        result.append(", ");
      }

      if (result.length() > 0) {
        result.delete(result.length() - 2, result.length());
      }

      return result.toString();
    }

    public String getValueNotNull() {
      final String value = getValue();

      return (value == null) ? "" : value;
    }

    @Override
    public HeaderField clone() {
      List<String> newValues = new ArrayList<String>();
      newValues.addAll(values);

      return new HeaderField(fieldName, newValues, lineNumber);
    }

    public int getLineNumber() {
      return lineNumber;
    }

    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
      result = prime * result + lineNumber;
      result = prime * result + ((values == null) ? 0 : values.hashCode());
      return result;
    }

    @Override
    public boolean equals(final Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      HeaderField other = (HeaderField) obj;
      if (fieldName == null) {
        if (other.fieldName != null) {
          return false;
        }
      } else if (!fieldName.equals(other.fieldName)) {
        return false;
      }
      if (lineNumber != other.lineNumber) {
        return false;
      }
      if (values == null) {
        if (other.values != null) {
          return false;
        }
      } else if (!values.equals(other.values)) {
        return false;
      }
      return true;
    }
  }
}
