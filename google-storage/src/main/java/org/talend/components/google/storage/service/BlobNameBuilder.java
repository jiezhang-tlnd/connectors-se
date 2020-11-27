/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.components.google.storage.service;

import java.io.Serializable;
import java.util.UUID;

public class BlobNameBuilder implements Serializable {

    private static final long serialVersionUID = -3957291260058538741L;

    private static final int UUID_STRING_SIZE = 36;

    public String generateName(final String begin) {
        return begin + '_' + UUID.randomUUID().toString();
    }

    public boolean isGenerated(final String begin, final String name) {
        return name.startsWith(begin + '_') && name.length() == begin.length() + 1 + UUID_STRING_SIZE
                && this.isUUID(name.substring(begin.length() + 1));
    }

    private boolean isUUID(final String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
