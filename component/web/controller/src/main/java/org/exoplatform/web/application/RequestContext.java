/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.web.application;

import org.exoplatform.services.resources.Orientation;
import org.exoplatform.web.url.LocatorProvider;
import org.exoplatform.web.url.ResourceLocator;
import org.exoplatform.web.url.ResourceType;
import org.exoplatform.web.url.ResourceURL;

import java.io.Writer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Created by The eXo Platform SAS
 * May 7, 2006
 * 
 * This abstract class is a wrapper on top of the request information such as the Locale in use,
 * the application (for instance PortalApplication, PortletApplication...), an access to the JavascriptManager
 * as well as a reference to the URLBuilder in use.
 * 
 * It also contains a ThreadLocal object for an easy access.
 * 
 *  Context can be nested and hence a getParentAppRequestContext() is also available
 * 
 */
abstract public class RequestContext
{

   final static public String ACTION = "op";

   private static ThreadLocal<RequestContext> tlocal_ = new ThreadLocal<RequestContext>();

   private Application app_;

   protected final RequestContext parentAppRequestContext_;

   private Map<String, Object> attributes;

   public RequestContext(Application app)
   {
      this.app_ = app;
      this.parentAppRequestContext_ = null;
   }

   protected RequestContext(RequestContext parentAppRequestContext, Application app_)
   {
      this.parentAppRequestContext_ = parentAppRequestContext;
      this.app_ = app_;
   }

   public Application getApplication()
   {
      return app_;
   }

   public Locale getLocale()
   {
      return parentAppRequestContext_.getLocale();
   }

   /**
    * Returns the locator factory associated with this context.
    *
    * @return the locator factory
    */
   public abstract LocatorProvider getLocatorProvider();

   public abstract <R, L extends ResourceLocator<R>> ResourceURL<R, L> newURL(ResourceType<R, L> resourceType, L locator);

   public final <R, L extends ResourceLocator<R>> ResourceURL<R, L> createURL(ResourceType<R, L> resourceType, R resource)
   {
      ResourceURL<R, L> url = createURL(resourceType);

      // Set the resource on the URL
      url.setResource(resource);

      //
      return url;
   }

   public final <R, L extends ResourceLocator<R>> ResourceURL<R, L> createURL(ResourceType<R, L> resourceType)
   {
      // Get the provider
      LocatorProvider provider = getLocatorProvider();

      // Obtain a locator for the resource type
      L locator = provider.newLocator(resourceType);

      //
      if (locator == null)
      {
         throw new IllegalArgumentException("No resource locator found for the resource type " + resourceType);
      }

      // Create an URL from the locator
      return newURL(resourceType, locator);
   }

   /**
    * Returns the orientation for the current request.
    *
    * @return the orientation
    */
   public abstract Orientation getOrientation();

   public ResourceBundle getApplicationResourceBundle()
   {
      return null;
   }

   abstract public String getRequestParameter(String name);

   abstract public String[] getRequestParameterValues(String name);

   public JavascriptManager getJavascriptManager()
   {
      return getParentAppRequestContext().getJavascriptManager();
   }

   abstract public URLBuilder<?> getURLBuilder();

   public String getRemoteUser()
   {
      return parentAppRequestContext_.getRemoteUser();
   }

   public boolean isUserInRole(String roleUser)
   {
      return parentAppRequestContext_.isUserInRole(roleUser);
   }

   abstract public boolean useAjax();

   public boolean getFullRender()
   {
      return true;
   }

   public ApplicationSession getApplicationSession()
   {
      throw new RuntimeException("This method is not supported");
   }

   public Writer getWriter() throws Exception
   {
      return parentAppRequestContext_.getWriter();
   }
   
   public void setWriter (Writer writer)
   {
	   parentAppRequestContext_.setWriter(writer);
   }

   final public Object getAttribute(String name)
   {
      if (attributes == null)
         return null;
      return attributes.get(name);
   }

   final public void setAttribute(String name, Object value)
   {
      if (attributes == null)
         attributes = new HashMap<String, Object>();
      attributes.put(name, value);
   }

   final public Object getAttribute(Class type)
   {
      return getAttribute(type.getName());
   }

   final public void setAttribute(Class type, Object value)
   {
      setAttribute(type.getName(), value);
   }

   public RequestContext getParentAppRequestContext()
   {
      return parentAppRequestContext_;
   }

   @SuppressWarnings("unchecked")
   public static <T extends RequestContext> T getCurrentInstance()
   {
      return (T)tlocal_.get();
   }

   public static void setCurrentInstance(RequestContext ctx)
   {
      tlocal_.set(ctx);
   }

}