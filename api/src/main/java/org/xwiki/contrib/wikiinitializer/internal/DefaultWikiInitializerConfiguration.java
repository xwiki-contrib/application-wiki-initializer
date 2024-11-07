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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
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
    private static final String CONTEXT_PATH = "contextPath";

    private static final String COOKIES = "cookies";

    private static final String DOT = ".";

    private static final String HEADERS = "headers";

    private static final String PARAMETERS = "parameters";

    private static final String REMOTE_ADDR = "remoteAddress";

    private static final String URL = "url";

    private static final String XWIKI = "xwiki";

    private static final String CONFIGURATION_PREFIX = "wikiInitializer.";

    private static final String INITIAL_REQUEST_PREFIX = CONFIGURATION_PREFIX + "initialRequest.";

    private static final String KEY_INITIALIZE_MAIN_WIKI = CONFIGURATION_PREFIX + "initializeMainWiki";

    private static final String KEY_INITIALIZE_SUB_WIKIS = CONFIGURATION_PREFIX + "initializeSubWikis";

    private static final String KEY_START_DISTRIBUTION_WIZARD_ON_INIT = CONFIGURATION_PREFIX
        + "startDistributionWizardOnInit";

    private static final String KEY_INITIALIZABLE_SUB_WIKIS = CONFIGURATION_PREFIX + "initializableSubWikis";

    private static final String INITIAL_REQUEST_URL = INITIAL_REQUEST_PREFIX + URL;

    private static final String LEGACY_INITIAL_REQUEST_URL = INITIAL_REQUEST_PREFIX + XWIKI + DOT + URL;

    private static final String INITIAL_REQUEST_CONTEXT_PATH = INITIAL_REQUEST_PREFIX + CONTEXT_PATH;

    private static final String LEGACY_INITIAL_REQUEST_CONTEXT_PATH =
        INITIAL_REQUEST_PREFIX + XWIKI + DOT + CONTEXT_PATH;

    private static final String INITIAL_REQUEST_PARAMETERS = INITIAL_REQUEST_PREFIX + PARAMETERS;

    private static final String LEGACY_INITIAL_REQUEST_PARAMETERS = INITIAL_REQUEST_PREFIX + XWIKI + DOT + PARAMETERS;

    private static final String INITIAL_REQUEST_HEADERS = INITIAL_REQUEST_PREFIX + HEADERS;

    private static final String LEGACY_INITIAL_REQUEST_HEADERS = INITIAL_REQUEST_PREFIX + XWIKI + DOT + HEADERS;

    private static final String INITIAL_REQUEST_COOKIES = INITIAL_REQUEST_PREFIX + COOKIES;

    private static final String LEGACY_INITIAL_REQUEST_COOKIES = INITIAL_REQUEST_PREFIX + XWIKI + DOT + COOKIES;

    private static final String INITIAL_REQUEST_REMOTE_ADDR = INITIAL_REQUEST_PREFIX + REMOTE_ADDR;

    private static final String LEGACY_INITIAL_REQUEST_REMOTE_ADDR = INITIAL_REQUEST_PREFIX + XWIKI + DOT + REMOTE_ADDR;

    private static final String VALUE_SUFFIX = ".value";

    private static final String LEGACY_PROPERTY_WARNING = "Configuration key [{}] is deprecated and may be removed in"
        + " a future release, please use [{}] instead.";

    @Inject
    @Named("xwikiproperties")
    private ConfigurationSource configuration;

    @Inject
    private Logger logger;

    @Override
    public boolean initializeMainWiki()
    {
        return configuration.getProperty(KEY_INITIALIZE_MAIN_WIKI, false);
    }

    @Override
    public boolean initializeAllSubWikis()
    {
        return configuration.getProperty(KEY_INITIALIZE_SUB_WIKIS, false);
    }

    @Override
    public boolean startDistributionWizardOnInitialization()
    {
        return configuration.getProperty(KEY_START_DISTRIBUTION_WIZARD_ON_INIT, false);
    }

    @Override
    public URL getInitialRequestURL()
    {
        return getProperty(INITIAL_REQUEST_URL, LEGACY_INITIAL_REQUEST_URL, java.net.URL.class);
    }

    @Override
    public String getInitialRequestContextPath()
    {
        return getProperty(INITIAL_REQUEST_CONTEXT_PATH, LEGACY_INITIAL_REQUEST_CONTEXT_PATH, String.class);
    }

    @Override
    public Map<String, List<String>> getInitialRequestParameters()
    {
        List<String> parameterNames = getProperty(INITIAL_REQUEST_PARAMETERS, LEGACY_INITIAL_REQUEST_PARAMETERS,
            new ArrayList<>(0));

        String key = (StringUtils.isNotBlank(configuration.getProperty(LEGACY_INITIAL_REQUEST_PARAMETERS)))
            ? LEGACY_INITIAL_REQUEST_PARAMETERS : INITIAL_REQUEST_PARAMETERS;

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
    public Map<String, List<String>> getInitialRequestHeaders()
    {
        List<String> headerNames = getProperty(INITIAL_REQUEST_HEADERS, LEGACY_INITIAL_REQUEST_HEADERS,
            new ArrayList<>(0));

        String key = (StringUtils.isNotBlank(configuration.getProperty(LEGACY_INITIAL_REQUEST_HEADERS)))
            ? LEGACY_INITIAL_REQUEST_HEADERS : INITIAL_REQUEST_HEADERS;

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
    public List<Cookie> getInitialRequestCookies()
    {
        List<String> cookieNames = getProperty(INITIAL_REQUEST_COOKIES, LEGACY_INITIAL_REQUEST_COOKIES,
            new ArrayList<>(0));

        String key = (StringUtils.isNotBlank(configuration.getProperty(LEGACY_INITIAL_REQUEST_COOKIES)))
            ? LEGACY_INITIAL_REQUEST_COOKIES : INITIAL_REQUEST_COOKIES;

        return cookieNames.stream().map(cookieName -> new Cookie(cookieName,
            configuration.getProperty(key + DOT + cookieName + VALUE_SUFFIX))).collect(Collectors.toList());
    }

    @Override
    public String getInitialRequestRemoteAddr()
    {
        return getProperty(INITIAL_REQUEST_REMOTE_ADDR, LEGACY_INITIAL_REQUEST_REMOTE_ADDR, String.class);
    }

    @Override
    public Set<WikiDescriptor> getInitializableWikis()
    {
        List<String> wikiIDs = configuration.getProperty(KEY_INITIALIZABLE_SUB_WIKIS, Collections.emptyList());
        return wikiIDs.stream().map(wikiID -> new WikiDescriptor(wikiID, wikiID)).collect(Collectors.toSet());
    }

    private <T> T getProperty(String key, String legacyKey, Class<T> valueClass)
    {
        T value = configuration.getProperty(key, valueClass);

        if (value == null) {
            value = configuration.getProperty(legacyKey, valueClass);

            if (value != null) {
                logger.warn(LEGACY_PROPERTY_WARNING, legacyKey, key);
            }
        }

        return value;
    }

    private <T> T getProperty(String key, String legacyKey, T def)
    {
        T value = configuration.getProperty(key, def);

        if (value == null) {
            value = configuration.getProperty(legacyKey, def);

            if (value != null) {
                logger.warn(LEGACY_PROPERTY_WARNING, legacyKey, key);
            }
        }

        return value;
    }
}
