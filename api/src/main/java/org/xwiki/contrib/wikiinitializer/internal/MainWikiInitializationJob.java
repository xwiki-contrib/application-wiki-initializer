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

import javax.inject.Inject;
import javax.inject.Named;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.InstantiationStrategy;
import org.xwiki.component.descriptor.ComponentInstantiationStrategy;
import org.xwiki.contrib.wikiinitializer.WikiInitializationManager;
import org.xwiki.contrib.wikiinitializer.WikiInitializerConfiguration;
import org.xwiki.job.AbstractJob;
import org.xwiki.job.DefaultJobStatus;
import org.xwiki.job.DefaultRequest;

/**
 * Job responsible to delay the initialization of the main wiki until all ApplicationStartedEvent event listeners
 * have finished processing.
 *
 * We absolutely need to wait for these events to be processed in order for the XWiki context to be properly
 * initialized.
 *
 * @version $Id$
 * @since 1.2.0
 */
@Component
@InstantiationStrategy(ComponentInstantiationStrategy.PER_LOOKUP)
@Named(MainWikiInitializationJob.JOB_TYPE)
public class MainWikiInitializationJob extends AbstractJob<DefaultRequest, DefaultJobStatus<DefaultRequest>>
{
    /**
     * The job name.
     */
    public static final String JOB_TYPE = "wikiInitializer/mainWiki";

    @Inject
    private WikiInitializerConfiguration wikiInitializerConfiguration;

    @Inject
    private WikiInitializationManager wikiInitializationManager;

    @Override
    public String getType()
    {
        return JOB_TYPE;
    }

    @Override
    protected void runInternal() throws Exception
    {
        Thread.sleep(wikiInitializerConfiguration.getMainWikiInitializationDelay());

        wikiInitializationManager.initialize(null);
    }
}
