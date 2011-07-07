/*
 * Copyright (C) 2011 eXo Platform SAS.
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

package org.exoplatform.portal.config;

import org.exoplatform.component.test.AbstractGateInTest;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.component.test.KernelBootstrap;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.portal.mop.SiteKey;
import org.exoplatform.portal.mop.importer.ImportMode;
import org.exoplatform.portal.mop.importer.Imported;
import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.mop.navigation.Node;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeModel;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.gatein.mop.api.workspace.Workspace;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public abstract class AbstractImportTest extends AbstractGateInTest
{

   protected abstract ImportMode getMode();

   protected abstract void assertOverride(NodeContext<?> root);

   protected abstract void assertNoOverride(NodeContext<?> root);

   public void testOverride() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport-configuration.xml");

      //
      System.setProperty("override", "true");
      System.setProperty("import.mode", getMode().toString());

      //
      System.setProperty("import.portal", "merge1");
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      NavigationService service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      RequestLifeCycle.begin(container);
      NavigationContext nav = service.loadNavigation(SiteKey.portal("classic"));
      NodeContext<?> root = service.loadNode(Node.MODEL, nav, Scope.ALL, null);
      assertEquals(1, root.getNodeCount());
      assertNotNull(root.get("foo"));
      RequestLifeCycle.end();
      bootstrap.dispose();

      //
      System.setProperty("import.portal", "merge2");
      bootstrap.boot();
      container = bootstrap.getContainer();
      service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      RequestLifeCycle.begin(container);
      nav = service.loadNavigation(SiteKey.portal("classic"));
      root = service.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      assertOverride(root);
      RequestLifeCycle.end();
      bootstrap.dispose();
   }

   public void testNoOverride() throws Exception
   {
      KernelBootstrap bootstrap = new KernelBootstrap();
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.test.jcr-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.identity-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "conf/exo.portal.component.portal-configuration.xml");
      bootstrap.addConfiguration(ContainerScope.PORTAL, "org/exoplatform/portal/config/TestImport-configuration.xml");

      //
      System.setProperty("override", "false");
      System.setProperty("import.mode", getMode().toString());

      //
      System.setProperty("import.portal", "merge1");
      bootstrap.boot();
      PortalContainer container = bootstrap.getContainer();
      NavigationService service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      RequestLifeCycle.begin(container);
      NavigationContext nav = service.loadNavigation(SiteKey.portal("classic"));
      NodeContext<?> root = service.loadNode(Node.MODEL, nav, Scope.ALL, null);
      assertEquals(1, root.getNodeCount());
      assertNotNull(root.get("foo"));
      RequestLifeCycle.end();
      bootstrap.dispose();

      //
      System.setProperty("import.portal", "merge2");
      bootstrap.boot();
      container = bootstrap.getContainer();
      service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      POMSessionManager mgr = (POMSessionManager)container.getComponentInstanceOfType(POMSessionManager.class);
      RequestLifeCycle.begin(container);
      nav = service.loadNavigation(SiteKey.portal("classic"));
      root = service.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      assertEquals(1, root.getNodeCount());
      assertNotNull(root.get("foo"));
      Workspace workspace = mgr.getSession().getWorkspace();
      assertTrue(workspace.isAdapted(Imported.class));
      workspace.removeAdapter(Imported.class);
      assertFalse(workspace.isAdapted(Imported.class));
      mgr.getSession().save();
      RequestLifeCycle.end();
      bootstrap.dispose();

      //
      bootstrap.boot();
      container = bootstrap.getContainer();
      service = (NavigationService)container.getComponentInstanceOfType(NavigationService.class);
      RequestLifeCycle.begin(container);
      nav = service.loadNavigation(SiteKey.portal("classic"));
      root = service.loadNode(NodeModel.SELF_MODEL, nav, Scope.ALL, null);
      assertNoOverride(root);
      RequestLifeCycle.end();
      bootstrap.dispose();
   }
}
