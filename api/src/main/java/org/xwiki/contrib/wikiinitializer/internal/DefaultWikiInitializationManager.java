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

import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.xwiki.bridge.event.ActionExecutingEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.servlet.ServletContainerException;
import org.xwiki.container.servlet.ServletContainerInitializer;
import org.xwiki.contrib.wikiinitializer.WikiInitializationException;
import org.xwiki.contrib.wikiinitializer.WikiInitializationManager;
import org.xwiki.contrib.wikiinitializer.WikiInitializerConfiguration;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.observation.ObservationManager;
import org.xwiki.wiki.descriptor.WikiDescriptor;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiEngineContext;
import com.xpn.xwiki.web.XWikiServletContext;
import com.xpn.xwiki.web.XWikiServletRequestStub;
import com.xpn.xwiki.web.XWikiServletResponseStub;

/**
 * Default implementation of {@link WikiInitializationManager}.
 *
 * @version $Id$
 * @since 1.2.0
 */
@Component
@Singleton
public class DefaultWikiInitializationManager implements WikiInitializationManager
{
    private static final String ACTION_VIEW = "view";

    private static final String ACTION_DISTRIBUTION = "distribution";

    @Inject
    private Environment environment;

    @Inject
    private Logger logger;

    @Inject
    private ServletContainerInitializer containerInitializer;

    @Inject
    private WikiInitializerConfiguration configuration;

    @Inject
    private ObservationManager observationManager;

    @Inject
    private Provider<XWikiContext> contextProvider;

    @Override
    public void initialize(WikiDescriptor descriptor) throws WikiInitializationException
    {
        String wikiId = (descriptor != null) ? descriptor.getId() : XWiki.DEFAULT_MAIN_WIKI;
        logger.info("Initializing wiki [{}] ...", wikiId);

        try {
            if (wikiId.equals(XWiki.DEFAULT_MAIN_WIKI)) {
                initializeMainWiki();
            } else {
                initializeSubWiki(descriptor);
            }
        } catch (Exception e) {
            throw new WikiInitializationException(String.format("Failed to initialize wiki with ID [%s]", wikiId), e);
        }
    }

    private void initializeMainWiki() throws XWikiException, ServletContainerException
    {
        ServletEnvironment servletEnvironment = (ServletEnvironment) environment;
        XWikiEngineContext engineContext = new XWikiServletContext(servletEnvironment.getServletContext());
        String action = configuration.startDistributionWizardOnInitialization() ? ACTION_DISTRIBUTION : ACTION_VIEW;

        XWikiServletRequestStub.Builder requestBuilder = new XWikiServletRequestStub.Builder();
        requestBuilder.setRequestURL(configuration.getInitialRequestURL());

        String contextPath = configuration.getInitialRequestContextPath();
        if (contextPath == null) {
            contextPath = servletEnvironment.getServletContext().getContextPath();
        }
        requestBuilder.setContextPath(contextPath);

        requestBuilder.setRequestParameters(
            configuration.getInitialRequestParameters().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toArray(new String[0]))));
        requestBuilder.setCookies(configuration.getInitialRequestCookies().toArray(new Cookie[0]));
        requestBuilder.setHeaders(configuration.getInitialRequestHeaders());
        requestBuilder.setRemoteAddr(configuration.getInitialRequestRemoteAddr());

        XWikiContext context = Utils.prepareContext(action, requestBuilder.build(),
            new XWikiServletResponseStub(), engineContext);
        context.setMode(XWikiContext.MODE_SERVLET);

        containerInitializer.initializeRequest(context.getRequest().getHttpServletRequest(), context);
        containerInitializer.initializeResponse(context.getResponse());
        containerInitializer.initializeSession(context.getRequest().getHttpServletRequest());

        XWiki xwiki = XWiki.getXWiki(configuration.startDistributionWizardOnInitialization(), context);

        if (configuration.startDistributionWizardOnInitialization()) {
            try {
                observationManager.notify(new ActionExecutingEvent(ACTION_DISTRIBUTION),
                    xwiki.getDocument(xwiki.getDefaultPage(context), context), context);
            } catch (Exception e) {
                logger.error("Failed to auto-start XWiki Distribution", e);
            }
        }
    }

    private void initializeSubWiki(WikiDescriptor descriptor) throws XWikiException, WikiInitializationException
    {
        XWikiContext context = contextProvider.get();
        if (context != null && context.getWiki() != null) {
            context.getWiki().initializeWiki(descriptor.getId(), false, context);
        } else {
            throw new WikiInitializationException(String.format("Invalid context or wiki found when initializing [%s]",
                descriptor.getId()));
        }
    }
}
