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
package ec.edu.cedia.redi.unesco.model;

import org.openrdf.model.URI;

/**
 *
 * @author Xavier Sumba <xavier.sumba93@ucuenca.ec>
 */
public class UnescoHierarchy {

    private String lvl2;
    private String lvl4;
    private String lvls6;
    private URI lvl2Uri;
    private URI lvl4Uri;

    public UnescoHierarchy(String lvl2, String lvl4, String lvls6, URI lvl2Uri, URI lvl4Uri) {
        this.lvl2 = lvl2;
        this.lvl4 = lvl4;
        this.lvls6 = lvls6;
        this.lvl2Uri = lvl2Uri;
        this.lvl4Uri = lvl4Uri;
    }

    public String getLevel2() {
        return lvl2;
    }

    public String getLevel4() {
        return lvl4;
    }

    public String getLevels6() {
        return lvls6;
    }

    public URI getLevel2Uri() {
        return lvl2Uri;
    }

    public URI getLevel4Uri() {
        return lvl4Uri;
    }

    @Override
    public String toString() {
        return lvl2Uri + " -> " + lvl4Uri
                + "\n\t\t" + lvl2
                + "\n\t\t" + lvl4
                + "\n\t\t" + lvls6;
    }
}
