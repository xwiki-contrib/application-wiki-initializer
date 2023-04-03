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

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.http.Cookie;

import org.slf4j.Logger;
import org.xwiki.bridge.event.WikiReadyEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.container.servlet.ServletContainerInitializer;
import org.xwiki.contrib.wikiinitializer.WikiInitializerConfiguration;
import org.xwiki.environment.Environment;
import org.xwiki.environment.internal.ServletEnvironment;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.wiki.descriptor.WikiDescriptor;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.web.Utils;
import com.xpn.xwiki.web.XWikiEngineContext;
import com.xpn.xwiki.web.XWikiServletContext;
import com.xpn.xwiki.web.XWikiServletRequestStub;
import com.xpn.xwiki.web.XWikiServletResponseStub;

/**
 * Listener that will automatically start the wiki initialization job.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named(WikiInitializationListener.LISTENER_NAME)
public class WikiInitializationListener extends AbstractEventListener
{
    /**
     * The listener name.
     */
    public static final String LISTENER_NAME = "WikiInitializationListener";

    private static final String ACTION_VIEW = "view";

    private static final String ACTION_DISTRIBUTION = "distribution";

    private static final String XWIKI = "xwiki";

    @Inject
    private Environment environment;

    @Inject
    private Logger logger;

    @Inject
    private ServletContainerInitializer containerInitializer;

    @Inject
    private WikiInitializerConfiguration configuration;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    /**
     * Create a new {@link WikiInitializationListener}.
     */
    public WikiInitializationListener()
    {
        super(LISTENER_NAME, new ApplicationStartedEvent(), new WikiReadyEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ApplicationStartedEvent && configuration.initializeMainWiki()) {
            initializeWiki(null);
        } else if (event instanceof WikiReadyEvent && XWIKI.equals(source)) {
            try {
                Collection<WikiDescriptor> wikisToInitialize = (configuration.initializeAllSubWikis())
                    ? wikiDescriptorManager.getAll()
                    : configuration.getInitializableWikis();

                for (WikiDescriptor descriptor : wikisToInitialize) {
                    initializeWiki(descriptor);
                }
            } catch (WikiManagerException e) {
                logger.error("Failed to initialize sub-wikis", e);
            }
        }
    }

    private void initializeWiki(WikiDescriptor descriptor)
    {
        String wikiId = (descriptor != null) ? descriptor.getId() : XWIKI;
        logger.info("Initializing wiki [{}] ...", wikiId);

        try {
            ServletEnvironment servletEnvironment = (ServletEnvironment) environment;
            XWikiEngineContext engineContext = new XWikiServletContext(servletEnvironment.getServletContext());

            XWikiServletRequestStub.Builder requestBuilder = new XWikiServletRequestStub.Builder();
            requestBuilder.setRequestURL(configuration.getInitialRequestURL(descriptor));
            requestBuilder.setContextPath(configuration.getInitialRequestContextPath(descriptor));
            requestBuilder.setRequestParameters(
                configuration.getInitialRequestParameters(descriptor).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().toArray(new String[0]))));
            requestBuilder.setCookies(configuration.getInitialRequestCookies(descriptor).toArray(new Cookie[0]));
            requestBuilder.setHeaders(configuration.getInitialRequestHeaders(descriptor));
            requestBuilder.setRemoteAddr(configuration.getInitialRequestRemoteAddr(descriptor));

            XWikiContext context = Utils.prepareContext(
                configuration.startDistributionWizardOnInitialization() ? ACTION_DISTRIBUTION : ACTION_VIEW,
                requestBuilder.build(), new XWikiServletResponseStub(), engineContext);
            context.setMode(XWikiContext.MODE_SERVLET);

            containerInitializer.initializeRequest(context.getRequest().getHttpServletRequest(), context);
            containerInitializer.initializeResponse(context.getResponse());
            containerInitializer.initializeSession(context.getRequest().getHttpServletRequest());

            XWiki.getXWiki(false, context);
        } catch (Exception e) {
            logger.error("Failed to auto-start XWiki", e);
        }
    }
}
