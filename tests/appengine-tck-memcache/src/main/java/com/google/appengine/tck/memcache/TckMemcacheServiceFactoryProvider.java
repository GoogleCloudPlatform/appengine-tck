/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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

package com.google.appengine.tck.memcache;

import com.google.appengine.api.memcache.AsyncMemcacheService;
import com.google.appengine.api.memcache.IMemcacheServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.spi.FactoryProvider;
import com.google.appengine.spi.ServiceProvider;
import org.jboss.capedwarf.aspects.proxy.AspectFactory;
import org.jboss.capedwarf.common.spi.CapedwarfFactoryProvider;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(FactoryProvider.class)
@ServiceProvider(value = IMemcacheServiceFactory.class, precedence = CapedwarfFactoryProvider.PRECEDENCE)
public class TckMemcacheServiceFactoryProvider extends CapedwarfFactoryProvider<IMemcacheServiceFactory> {
    private final IMemcacheServiceFactory factory = new IMemcacheServiceFactory() {
        public MemcacheService getMemcacheService(String s) {
            return AspectFactory.createProxy(MemcacheService.class, new TckMemcacheService(s));
        }

        public AsyncMemcacheService getAsyncMemcacheService(String s) {
            return AspectFactory.createProxy(AsyncMemcacheService.class, new TckAsyncMemcacheService(s));
        }
    };

    public TckMemcacheServiceFactoryProvider() {
        super(IMemcacheServiceFactory.class);
    }

    protected IMemcacheServiceFactory getFactoryInstance() {
        return factory;
    }
}