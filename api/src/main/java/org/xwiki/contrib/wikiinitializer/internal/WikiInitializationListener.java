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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.xwiki.bridge.event.ApplicationReadyEvent;
import org.xwiki.component.annotation.Component;
import org.xwiki.contrib.wikiinitializer.WikiInitializationException;
import org.xwiki.contrib.wikiinitializer.WikiInitializationManager;
import org.xwiki.contrib.wikiinitializer.WikiInitializerConfiguration;
import org.xwiki.observation.AbstractEventListener;
import org.xwiki.observation.event.ApplicationStartedEvent;
import org.xwiki.observation.event.Event;
import org.xwiki.wiki.descriptor.WikiDescriptor;
import org.xwiki.wiki.descriptor.WikiDescriptorManager;
import org.xwiki.wiki.manager.WikiManagerException;

import com.xpn.xwiki.XWiki;

/**
 * Listener that will automatically start the wiki initialization job.
 *
 * @version $Id$
 * @since 1.0
 */
@Component
@Singleton
@Named(WikiInitializationListener.LISTENER_NAME)
@Priority(Integer.MAX_VALUE)
public class WikiInitializationListener extends AbstractEventListener
{
    /**
     * The listener name.
     */
    public static final String LISTENER_NAME = "WikiInitializationListener";

    @Inject
    private Logger logger;

    @Inject
    private WikiInitializerConfiguration configuration;

    @Inject
    private WikiDescriptorManager wikiDescriptorManager;

    @Inject
    private WikiInitializationManager wikiInitializationManager;

    /**
     * Create a new {@link WikiInitializationListener}.
     */
    public WikiInitializationListener()
    {
        super(LISTENER_NAME, new ApplicationStartedEvent(), new ApplicationReadyEvent());
    }

    @Override
    public void onEvent(Event event, Object source, Object data)
    {
        if (event instanceof ApplicationStartedEvent && configuration.initializeMainWiki()) {
            try {
                wikiInitializationManager.initialize(null);
            } catch (WikiInitializationException e) {
                logger.error("Failed to initialize main wiki", e);
            }
        } else if (event instanceof ApplicationReadyEvent) {
            try {
                Collection<WikiDescriptor> wikisToInitialize = (configuration.initializeAllSubWikis())
                    ? wikiDescriptorManager.getAll()
                    : configuration.getInitializableWikis();

                for (WikiDescriptor descriptor : wikisToInitialize) {
                    if (!XWiki.DEFAULT_MAIN_WIKI.equals(descriptor.getId())) {
                        wikiInitializationManager.initialize(descriptor);
                    }
                }
            } catch (WikiManagerException | WikiInitializationException e) {
                logger.error("Failed to initialize sub-wikis", e);
            }
        }
    }
}
