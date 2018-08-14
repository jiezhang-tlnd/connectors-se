package org.talend.components.magentocms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.talend.sdk.component.api.configuration.Option;
import org.talend.sdk.component.api.configuration.condition.ActiveIf;
import org.talend.sdk.component.api.configuration.type.DataStore;
import org.talend.sdk.component.api.configuration.ui.layout.GridLayout;
import org.talend.sdk.component.api.meta.Documentation;

import java.io.Serializable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@DataStore("MagentoDataStore")
@GridLayout({ @GridLayout.Row({ "magentoWebServerUrl" }), @GridLayout.Row({ "magentoRestVersion" }),
        @GridLayout.Row({ "authenticationType" }), @GridLayout.Row({ "authenticationOauth1Settings" }),
        @GridLayout.Row({ "authenticationTokenSettings" }), @GridLayout.Row({ "authenticationLoginPasswordSettings" }) })
public class MagentoCmsConfigurationBase implements Serializable {

    @Option
    @Documentation("URL of web server (including port after ':'), e.g. 'http://mymagentoserver.com:1234'")
    private String magentoWebServerUrl;

    @Option
    @Documentation("The version of Magento REST ,e.g. 'V1'")
    private RestVersion magentoRestVersion;

    @Option
    @Documentation("authentication type (OAuth 1.0 or else)")
    private AuthenticationType authenticationType;

    @Option
    @Documentation("authentication OAuth 1.0 settings")
    @ActiveIf(target = "authenticationType", value = { "OAUTH_1" })
    private AuthenticationOauth1Settings authenticationOauth1Settings;

    @Option
    @Documentation("authentication Token settings")
    @ActiveIf(target = "authenticationType", value = { "AUTHENTICATION_TOKEN" })
    private AuthenticationTokenSettings authenticationTokenSettings;

    @Option
    @Documentation("authentication Login-Password settings")
    @ActiveIf(target = "authenticationType", value = { "LOGIN_PASSWORD" })
    private AuthenticationLoginPasswordSettings authenticationLoginPasswordSettings;

    public AuthenticationSettings getAuthSettings() throws UnknownAuthenticationTypeException {
        if (authenticationType == AuthenticationType.OAUTH_1) {
            return authenticationOauth1Settings;
        } else if (authenticationType == AuthenticationType.AUTHENTICATION_TOKEN) {
            return authenticationTokenSettings;
        } else if (authenticationType == AuthenticationType.LOGIN_PASSWORD) {
            return authenticationLoginPasswordSettings;
        }
        throw new UnknownAuthenticationTypeException();
    }
}