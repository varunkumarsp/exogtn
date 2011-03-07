/*
 * Copyright (C) 2010 eXo Platform SAS.
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

package org.exoplatform.portal.mop.navigation;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NodeContextModel<N> implements NodeContext<N>
{

   /** . */
   final N node;

   /** The original node context data. */
   final NodeData data;

   /** ; */
   final String name;

   /** The new state if any. */
   private NodeState state;

   /** . */
   N parent;

   /** . */
   Children children;


   NodeContextModel(NodeModel<N> model, NodeData data)
   {
      this.node = model.create(this);
      this.data = data;
      this.name = data.getName();
      this.state = data.getState();
      this.children = null;
   }

   NodeContextModel(NodeModel<N> model, String name, NodeState state)
   {
      this.node = model.create(this);
      this.data = null;
      this.name = name;
      this.state = state;
      this.children = new Children();
   }

   public String getId()
   {
      return data != null ? data.getId() : null;
   }

   public String getName()
   {
      return name;
   }

   public int getChildrenCount()
   {
      return data.getChildrenCount();
   }

   public NodeState getState()
   {
      return state;
   }

   public void setState(NodeState state)
   {
      this.state = state;
   }

   public N getParent()
   {
      return parent;
   }

   public N getChild(String childName)
   {
      if (children != null)
      {
         NodeContextModel<N> childCtx = children.get(childName);
         return childCtx != null ? childCtx.node : null;
      }
      else
      {
         return null;
      }
   }

   public Collection<N> getChildren()
   {
      return children;
   }

   public N addChild(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (children == null)
      {
         throw new IllegalStateException();
      }
      else if (children.contains(name))
      {
         throw new IllegalArgumentException();
      }


      //
      NodeContextModel<N> childCtx = new NodeContextModel<N>(model, name, new NodeState.Builder().capture());
      children.put(null, childCtx);
      childCtx.parent = node;

      //
      return childCtx.node;
   }

   public void addChild(NodeModel<N> model, Integer index, N child)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (children == null)
      {
         throw new IllegalStateException();
      }

      //
      children.put(index, (NodeContextModel<N>)model.getContext(child));
   }

   public boolean removeChild(NodeModel<N> model, String name)
   {
      if (model == null)
      {
         throw new NullPointerException();
      }
      if (name == null)
      {
         throw new NullPointerException();
      }
      if (children == null)
      {
         throw new IllegalStateException();
      }

      //
      return children.remove(name) != null;
   }

   void createChildren()
   {
      if (children == null)
      {
         children = new Children();
      }
      else
      {
         throw new IllegalStateException();
      }
   }

   class Children extends AbstractCollection<N>
   {

      /** . */
      final ArrayList<NodeContextModel<N>> values = new ArrayList<NodeContextModel<N>>();

      NodeContextModel<N> get(String name)
      {
         int size = values.size();
         for (int i = 0;i < size;i++)
         {
            NodeContextModel<N> ctx = values.get(i);
            if (ctx.name.equals(name))
            {
               return ctx;
            }
         }
         return null;
      }

      void put(Integer index, NodeContextModel<N> childCtx)
      {
         if (index == null)
         {
            index = values.size();
         }
         if (childCtx.parent != null)
         {
            if (childCtx.parent != node)
            {
               throw new UnsupportedOperationException("not supported");
            }
            int removedIndex = remove(childCtx.name);
            if (removedIndex < index)
            {
               index--;
            }
            values.add(index, childCtx);
         }
         else
         {
            if (contains(childCtx.name))
            {
               throw new IllegalArgumentException();
            }
            childCtx.parent = node;
            values.add(index, childCtx);
         }
      }

      Integer remove(String name)
      {
         int size = values.size();
         for (int i = 0;i < size;i++)
         {
            NodeContextModel<N> ctx = values.get(i);
            if (ctx.name.equals(name))
            {
               values.remove(i);
               return i;
            }
         }
         return null;
      }

      boolean contains(String name)
      {
         return get(name) != null;
      }

      @Override
      public Iterator<N> iterator()
      {
         final Iterator<NodeContextModel<N>> iterator = values.iterator();
         return new Iterator<N>()
         {
            public boolean hasNext()
            {
               return iterator.hasNext();
            }
            public N next()
            {
               return iterator.next().node;
            }
            public void remove()
            {
               throw new UnsupportedOperationException();
            }
         };
      }

      @Override
      public int size()
      {
         return values.size();
      }
   }
}