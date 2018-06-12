/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ec.edu.cedia.redi;

import org.openrdf.model.URI;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class Author {

    private  URI uri;
    private  String keywords;
    private  String topics; 

    public String getTopics() {
        return topics;
    }

    public void setTopics(String topics) {
        this.topics = topics;
    }

    public Author(URI author, String keywords) {
        this.uri = author;
        this.keywords = keywords;
    }

    public URI getURI() {
        return uri;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

}
