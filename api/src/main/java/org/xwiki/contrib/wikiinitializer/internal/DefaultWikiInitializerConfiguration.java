/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.contrib.wikiinitializer.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;

import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConfigurationSource;
import org.xwiki.contrib.wikiinitializer.WikiInitializerConfiguration;
import org.xwiki.wiki.descriptor.WikiDescriptor;

/**
 * Default implementation of {@link WikiInitializerConfiguration}.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
public class DefaultWikiInitializerConfiguration implements WikiInitializerConfiguration
{
    private static final String CONFIGURATION_PREFIX = "wikiinitializer.";

    private static final String INITIAL_REQUEST_PREFIX = CONFIGURATION_PREFIX + "initialRequest.";

    private static final String KEY_INITIALIZE_MAIN_WIKI = CONFIGURATION_PREFIX + "initializeMainWiki";

    private static final String KEY_INITIALIZE_SUB_WIKIS = CONFIGURATION_PREFIX + "initializeSubWikis";

    private static final String KEY_START_DISTRIBUTION_WIZARD_ON_INIT = CONFIGURATION_PREFIX
        + "startDistributionWizardOnInit";

    private static final String KEY_INITIALIZABLE_SUB_WIKIS = CONFIGURATION_PREFIX + "initializableSubWikis";

    private static final String INITIAL_REQUEST_URL = ".url";

    private static final String INITIAL_REQUEST_CONTEXT_PATH = ".contextPath";

    private static final String INITIAL_REQUEST_PARAMETERS = ".parameters";

    private static final String INITIAL_REQUEST_HEADERS = ".headers";

    private static final String INITIAL_REQUEST_COOKIES = ".cookies";

    private static final String INITIAL_REQUEST_REMOTE_ADDR = ".remoteAddress";

    private static final String VALUE_SUFFIX = ".value";

    private static final String DOT = ".";

    private static final String XWIKI = "xwiki";

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configuration;

    @Override
    public boolean initializeMainWiki()
    {
        return configuration.getProperty(KEY_INITIALIZE_MAIN_WIKI);
    }

    @Override
    public boolean initializeAllSubWikis()
    {
        return configuration.getProperty(KEY_INITIALIZE_SUB_WIKIS);
    }

    @Override
    public boolean startDistributionWizardOnInitialization()
    {
        return configuration.getProperty(KEY_START_DISTRIBUTION_WIZARD_ON_INIT);
    }

    @Override
    public URL getInitialRequestURL(WikiDescriptor descriptor)
    {
        String key = INITIAL_REQUEST_PREFIX + ((descriptor != null) ? descriptor.getId() : XWIKI) + INITIAL_REQUEST_URL;
        return configuration.getProperty(key, URL.class);
    }

    @Override
    public String getInitialRequestContextPath(WikiDescriptor descriptor)
    {
        String key = INITIAL_REQUEST_PREFIX + ((descriptor != null) ? descriptor.getId() : XWIKI)
            + INITIAL_REQUEST_CONTEXT_PATH;
        return configuration.getProperty(key);
    }

    @Override
    public Map<String, List<String>> getInitialRequestParameters(WikiDescriptor descriptor)
    {
        String key = INITIAL_REQUEST_PREFIX + ((descriptor != null) ? descriptor.getId() : XWIKI)
            + INITIAL_REQUEST_PARAMETERS;
        List<String> parameterNames = configuration.getProperty(key, new ArrayList<>(0));

        Map<String, List<String>> parameters = new HashMap<>();
        for (String parameterName : parameterNames) {
            if (!parameters.containsKey(parameterName)) {
                parameters.put(parameterName, new ArrayList<>());
            }

            parameters.get(parameterName).addAll(
                configuration.getProperty(key + DOT + parameterName + VALUE_SUFFIX, new ArrayList<>(0)));
        }

        return parameters;
    }

    @Override
    public Map<String, List<String>> getInitialRequestHeaders(WikiDescriptor descriptor)
    {
        String key = INITIAL_REQUEST_PREFIX + ((descriptor != null) ? descriptor.getId() : XWIKI)
            + INITIAL_REQUEST_HEADERS;
        List<String> headerNames = configuration.getProperty(key, new ArrayList<>(0));

        Map<String, List<String>> headers = new HashMap<>();
        for (String headerName : headerNames) {
            if (!headers.containsKey(headerName)) {
                headers.put(headerName, new ArrayList<>());
            }

            headers.get(headerName).addAll(
                configuration.getProperty(key + DOT + headerName + VALUE_SUFFIX, new ArrayList<>(0)));
        }

        return headers;
    }

    @Override
    public List<Cookie> getInitialRequestCookies(WikiDescriptor descriptor)
    {
        String key = INITIAL_REQUEST_PREFIX + ((descriptor != null) ? descriptor.getId() : XWIKI)
            + INITIAL_REQUEST_COOKIES;
        List<String> cookieNames = configuration.getProperty(key, new ArrayList<>(0));

        return cookieNames.stream().map(cookieName -> new Cookie(cookieName,
            configuration.getProperty(key + DOT + cookieName + VALUE_SUFFIX))).collect(Collectors.toList());
    }

    @Override
    public String getInitialRequestRemoteAddr(WikiDescriptor descriptor)
    {
        String key = INITIAL_REQUEST_PREFIX + ((descriptor != null) ? descriptor.getId() : XWIKI)
            + INITIAL_REQUEST_REMOTE_ADDR;
        return configuration.getProperty(key);
    }

    @Override
    public Set<WikiDescriptor> getInitializableWikis()
    {
        List<String> wikiIDs = configuration.getProperty(KEY_INITIALIZABLE_SUB_WIKIS, Collections.emptyList());
        return wikiIDs.stream().map(wikiID -> new WikiDescriptor(wikiID, wikiID)).collect(Collectors.toSet());
    }
}
