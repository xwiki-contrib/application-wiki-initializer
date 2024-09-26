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
package org.xwiki.contrib.wikiinitializer;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.xwiki.component.annotation.Role;
import org.xwiki.stability.Unstable;
import org.xwiki.wiki.descriptor.WikiDescriptor;

/**
 * Configuration interface for the Wiki Initializer application.
 *
 * @version $Id$
 * @since 1.0
 */
@Role
@Unstable
public interface WikiInitializerConfiguration
{
    /**
     * @return true if the main wiki should be automatically initialized
     */
    boolean initializeMainWiki();

    /**
     * @return true if every sub-wiki should be iniitalized
     */
    boolean initializeAllSubWikis();

    /**
     * @return true if the distribution wizard of each wiki should be started on wiki initialization
     */
    boolean startDistributionWizardOnInitialization();

    /**
     * @param descriptor the wiki to initialize. If the descriptor is null, the main wiki descriptor will be used.
     * @return the initialization URL to use
     */
    URL getInitialRequestURL(WikiDescriptor descriptor);

    /**
     * @param descriptor the wiki to initialize. If the descriptor is null, the main wiki descriptor will be used.
     * @return the context path to provide in the initialization request
     */
    String getInitialRequestContextPath(WikiDescriptor descriptor);

    /**
     * @param descriptor the wiki to initialize. If the descriptor is null, the main wiki descriptor will be used.
     * @return the initialization request parameters
     */
    Map<String, List<String>> getInitialRequestParameters(WikiDescriptor descriptor);

    /**
     * @param descriptor the wiki to initialize. If the descriptor is null, the main wiki descriptor will be used.
     * @return the initialization request headers
     */
    Map<String, List<String>> getInitialRequestHeaders(WikiDescriptor descriptor);

    /**
     * @param descriptor the wiki to initialize. If the descriptor is null, the main wiki descriptor will be used.
     * @return the initialization cookies
     */
    List<Cookie> getInitialRequestCookies(WikiDescriptor descriptor);

    /**
     * @param descriptor the wiki to initialize. If the descriptor is null, the main wiki descriptor will be used.
     * @return the initialization remote address
     */
    String getInitialRequestRemoteAddr(WikiDescriptor descriptor);

    /**
     * @return the list of every wiki that should be automatically initialized (used when
     * {@link #initializeAllSubWikis()} is false
     */
    Set<WikiDescriptor> getInitializableWikis();
}
