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
package org.talend.components.ftp.dataset;

import lombok.Data;
import org.talend.components.ftp.datastore.FTPDataStore;
import org.talend.sdk.component.api.component.Icon;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.type.DataSet;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Data
@DataSet("FtpDataset")
@Icon(value = Icon.IconType.CUSTOM, custom = "ftp")
@GridLayout(names = GridLayout.FormType.MAIN, value = { @GridLayout.Row("datastore"), @GridLayout.Row({ "folder" }),
        @GridLayout.Row({ "filePrefix" }) })
@GridLayout(names = GridLayout.FormType.ADVANCED, value = { @GridLayout.Row("listHiddenFiles"), @GridLayout.Row("binary"),
        @GridLayout.Row("encoding") })
public class FTPDataSet implements Serializable {

    @Option
    @Documentation("FTP datastore.")
    private FTPDataStore datastore;

    @Option
    @Documentation("Folder to work in.")
    private String folder = "";

    @Option
    @Documentation("File prefix filter")
    private String filePrefix;

    @Option
    @Documentation("Should hidden files be listed.")
    private boolean listHiddenFiles = false;

    @Option
    @Documentation("Activate binary mode, if false ascii mode is used.")
    private boolean binary;

    @Option
    @Documentation("Control encoding.")
    private String encoding = "ISO-8859-1";
}