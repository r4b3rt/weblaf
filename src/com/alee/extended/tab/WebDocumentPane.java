/*
 * This file is part of WebLookAndFeel library.
 *
 * WebLookAndFeel library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * WebLookAndFeel library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with WebLookAndFeel library.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.alee.extended.tab;

import com.alee.laf.panel.WebPanel;
import com.alee.laf.splitpane.WebSplitPane;
import com.alee.laf.tabbedpane.WebTabbedPane;
import com.alee.managers.drag.DragManager;
import com.alee.utils.CollectionUtils;
import com.alee.utils.TextUtils;
import com.alee.utils.swing.AncestorAdapter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This component can
 * This might be us
 *
 * @param <T> document type
 * @author Mikle Garin
 */

public class WebDocumentPane<T extends DocumentData> extends WebPanel implements SwingConstants
{
    protected static final String DATA_KEY = "document.pane.data";

    /**
     * Unique document pane ID.
     * Used to allow or disallow documents drag between different document panes.
     */
    protected final String id;

    /**
     * Root structure element.
     * Might either be PaneData or SplitData.
     */
    protected StructureData root;

    /**
     * Last active pane.
     */
    protected PaneData<T> activePane;

    /**
     * Whether documents can be closed or not.
     */
    protected boolean closeable = true;

    /**
     * Whether documents drag enabled or not.
     */
    protected boolean dragEnabled = true;

    /**
     * Whether documents drag between different document panes is enabled or not.
     */
    protected boolean dragBetweenPanesEnabled = false;

    /**
     * Whether split creation is enabled or not.
     */
    protected boolean splitEnabled = true;

    /**
     * Whether tab menu is enabled or not.
     */
    protected boolean tabMenuEnabled = true;

    public WebDocumentPane ()
    {
        super ( "document-pane" );

        // Generating unique document pane ID
        this.id = TextUtils.generateId ( "WDP" );

        // Add initial pane
        init ();

        // Registering drag view handler
        final DocumentDragViewHandler dragViewHandler = new DocumentDragViewHandler ( this );
        addAncestorListener ( new AncestorAdapter ()
        {
            @Override
            public void ancestorAdded ( final AncestorEvent event )
            {
                DragManager.registerViewHandler ( dragViewHandler );
            }

            @Override
            public void ancestorRemoved ( final AncestorEvent event )
            {
                DragManager.unregisterViewHandler ( dragViewHandler );
            }
        } );
    }

    public boolean isCloseable ()
    {
        return closeable;
    }

    public void setCloseable ( final boolean closeable )
    {
        this.closeable = closeable;
    }

    public boolean isDragEnabled ()
    {
        return dragEnabled;
    }

    public void setDragEnabled ( final boolean dragEnabled )
    {
        this.dragEnabled = dragEnabled;
    }

    public boolean isDragBetweenPanesEnabled ()
    {
        return dragBetweenPanesEnabled;
    }

    public void setDragBetweenPanesEnabled ( final boolean dragBetweenPanesEnabled )
    {
        this.dragBetweenPanesEnabled = dragBetweenPanesEnabled;
    }

    public boolean isSplitEnabled ()
    {
        return splitEnabled;
    }

    public void setSplitEnabled ( final boolean splitEnabled )
    {
        this.splitEnabled = splitEnabled;
    }

    public boolean isTabMenuEnabled ()
    {
        return tabMenuEnabled;
    }

    public void setTabMenuEnabled ( final boolean tabMenuEnabled )
    {
        this.tabMenuEnabled = tabMenuEnabled;
    }

    /**
     * Returns current root element data.
     * This is either SplitData or PaneData object.
     *
     * @return current root element data
     */
    public StructureData getStructureRoot ()
    {
        return root;
    }

    /**
     * Sets new root element data.
     * This call replaces all data stored in this document pane with new one.
     *
     * @param root new root element data
     */
    public void setStructureRoot ( final StructureData root )
    {
        // Clearing root component
        if ( this.root != null )
        {
            remove ( this.root.getComponent () );
        }

        // Initializing new root
        if ( root != null )
        {
            // Adding root component
            add ( root.getComponent (), BorderLayout.CENTER );

            // Changing root
            this.root = root;
            this.activePane = root.findClosestPane ();

            // Updating document pane view
            revalidate ();
            repaint ();
        }
        else
        {
            // Add initial pane
            init ();
        }
    }

    /**
     * Initializes root and active pane.
     */
    protected void init ()
    {
        // Creating data for root pane
        final PaneData rootPane = new PaneData<T> ();

        // Adding root pane
        add ( rootPane.getTabbedPane (), BorderLayout.CENTER );

        // Applying initial values
        root = rootPane;
        activePane = rootPane;
    }

    /**
     * Splits document's pane into two panes using the specified direction to decide split settings.
     *
     * @param movedDocument document that should be moved to new pane
     * @param direction     split direction
     */
    public void split ( final T movedDocument, final int direction )
    {
        final PaneData<T> pane = getPane ( movedDocument );
        if ( pane != null )
        {
            split ( pane, movedDocument, direction );
        }
    }

    /**
     * Splits specified pane into two panes using the specified direction to decide split settings.
     *
     * @param splittedPane  pane that will be splitted
     * @param movedDocument document that should be moved from splitted pane to new one
     * @param direction     split direction
     * @return second pane created in the split process
     */
    protected PaneData<T> split ( final PaneData<T> splittedPane, final T movedDocument, final int direction )
    {
        final PaneData<T> otherPane;
        if ( splittedPane != null )
        {
            // Choosing course of action depending on splitted pane parent
            final boolean ltr = direction == RIGHT || direction == BOTTOM;
            final int orientation = direction == LEFT || direction == RIGHT ? VERTICAL : HORIZONTAL;
            if ( splittedPane.getTabbedPane ().getParent () == WebDocumentPane.this )
            {
                // Creating data for new pane
                otherPane = new PaneData<T> ();

                // Saving sizes to restore split locations
                final Dimension size = splittedPane.getTabbedPane ().getSize ();

                // Adding root split
                final PaneData<T> first = ltr ? splittedPane : otherPane;
                final PaneData<T> last = ltr ? otherPane : splittedPane;
                final SplitData<T> splitData = new SplitData<T> ( orientation, first, last );
                remove ( splittedPane.getTabbedPane () );
                add ( splitData.getSplitPane (), BorderLayout.CENTER );

                // Restoring split locations
                splitData.getSplitPane ().setDividerLocation ( orientation == VERTICAL ? size.width / 2 : size.height / 2 );

                // Changing root
                root = splitData;
            }
            else
            {
                // Determining parent split
                final WebSplitPane parentSplit = ( WebSplitPane ) splittedPane.getTabbedPane ().getParent ();
                final SplitData<T> parentSplitData = getData ( parentSplit );
                if ( parentSplitData.getOrientation () == orientation && ltr && parentSplitData.getFirst () == splittedPane &&
                        parentSplitData.getLast () instanceof PaneData )
                {
                    // Using existing split and pane
                    otherPane = ( PaneData<T> ) parentSplitData.getLast ();
                }
                else if ( parentSplitData.getOrientation () == orientation && !ltr && parentSplitData.getLast () == splittedPane &&
                        parentSplitData.getFirst () instanceof PaneData )
                {
                    // Using existing split and pane
                    otherPane = ( PaneData<T> ) parentSplitData.getFirst ();
                }
                else
                {
                    // Creating data for new pane
                    otherPane = new PaneData<T> ();

                    // Saving sizes to restore split locations
                    final int parentSplitLocation = parentSplitData.getSplitPane ().getDividerLocation ();
                    final Dimension size = splittedPane.getTabbedPane ().getSize ();

                    // Adding inner split
                    final PaneData<T> first = ltr ? splittedPane : otherPane;
                    final PaneData<T> last = ltr ? otherPane : splittedPane;
                    final SplitData<T> splitData = new SplitData<T> ( orientation, first, last );
                    parentSplitData.replace ( splittedPane, splitData );

                    // Restoring split locations
                    splitData.getSplitPane ().setDividerLocation ( orientation == VERTICAL ? size.width / 2 : size.height / 2 );
                    parentSplitData.getSplitPane ().setDividerLocation ( parentSplitLocation );
                }
            }

            // Moving document to new pane if it is specified
            if ( movedDocument != null )
            {
                splittedPane.remove ( movedDocument );
                otherPane.add ( movedDocument );
            }

            // Updating document pane view
            revalidate ();
            repaint ();
        }
        else
        {
            // Its not possible to split unspecified pane
            otherPane = null;
        }
        return otherPane;
    }

    /**
     * Merges specified structure element and its sub-elements if it is possible.
     * If PaneData provided its parent split will be merged.
     * If SplitData provided it will be merged.
     *
     * @param toMerge structure element to merge
     */
    public void merge ( final StructureData toMerge )
    {
        // Retrieving split data that should be merged
        if ( toMerge instanceof PaneData )
        {
            // When pane is forced to merge with opposite
            final PaneData<T> mergedPane = ( PaneData<T> ) toMerge;
            final Container parent = mergedPane.getTabbedPane ().getParent ();

            // Merge only if actually inside of a split
            // Otherwise this is a root pane which can't be merged
            if ( parent instanceof WebSplitPane )
            {
                final WebSplitPane splitPane = ( WebSplitPane ) parent;
                mergeImpl ( getData ( splitPane ) );

                // Updating document pane view
                revalidate ();
                repaint ();
            }
        }
        else
        {
            // When split is forced to merge into single pane
            mergeImpl ( ( SplitData<T> ) toMerge );

            // Updating document pane view
            revalidate ();
            repaint ();
        }
    }

    protected void mergeImpl ( final SplitData<T> splitData )
    {
        final StructureData first = splitData.getFirst ();
        final StructureData last = splitData.getLast ();

        // Determining the resulting element
        final StructureData<T> result;
        if ( isEmptyPane ( first ) || isEmptyPane ( last ) )
        {
            result = isEmptyPane ( first ) ? last : first;
        }
        else
        {
            // Merge inner content first so we have split with tabs only inside
            if ( first instanceof SplitData )
            {
                mergeImpl ( ( SplitData<T> ) first );
            }
            if ( last instanceof SplitData )
            {
                mergeImpl ( ( SplitData<T> ) last );
            }

            // Moving all documents from second pane to first
            final PaneData<T> firstPane = ( PaneData<T> ) first;
            final PaneData<T> lastPane = ( PaneData<T> ) last;
            final PaneData<T> toPane = firstPane.count () > lastPane.count () ? firstPane : lastPane;
            final PaneData<T> fromPane = firstPane.count () > lastPane.count () ? lastPane : firstPane;
            for ( final T document : CollectionUtils.copy ( fromPane.getData () ) )
            {
                fromPane.remove ( document );
                toPane.add ( document );
            }
            result = toPane;
        }

        // Removing merged split
        final WebSplitPane splitPane = splitData.getSplitPane ();
        if ( splitPane.getParent () == WebDocumentPane.this )
        {
            // Removing root split and adding tab pane
            remove ( splitPane );
            add ( result.getComponent (), BorderLayout.CENTER );

            // Changing root
            root = result;
        }
        else
        {
            // Retrieving parent split
            final WebSplitPane parentSplit = ( WebSplitPane ) splitPane.getParent ();
            final SplitData<T> parentSplitData = getData ( parentSplit );
            final int dividerLocation = parentSplit.getDividerLocation ();

            // Changing parent split component
            if ( parentSplit.getLeftComponent () == splitPane )
            {
                parentSplitData.setFirst ( result );
            }
            else
            {
                parentSplitData.setLast ( result );
            }

            // Restoring divider location
            parentSplit.setDividerLocation ( dividerLocation );
        }
    }

    protected boolean isEmptyPane ( final StructureData data )
    {
        return data instanceof PaneData && ( ( PaneData<T> ) data ).count () == 0;
    }

    /**
     * Marks new pane as active one.
     *
     * @param paneData new active pane
     */
    protected void activate ( final PaneData<T> paneData )
    {
        activePane = paneData;
    }

    public T getSelectedDocument ()
    {
        return activePane != null ? activePane.getSelected () : null;
    }

    public T getDocument ( final int index )
    {
        return activePane != null ? activePane.get ( index ) : null;
    }

    public T getDocument ( final String id )
    {
        for ( final PaneData<T> paneData : getAllPanes () )
        {
            final T document = paneData.get ( id );
            if ( document != null )
            {
                return document;
            }
        }
        return null;
    }

    public List<PaneData<T>> getAllPanes ()
    {
        final List<PaneData<T>> panes = new ArrayList<PaneData<T>> ();
        collectPanes ( root, panes );
        return panes;
    }

    protected void collectPanes ( final StructureData structureData, final List<PaneData<T>> panes )
    {
        if ( structureData instanceof PaneData )
        {
            panes.add ( ( PaneData<T> ) structureData );
        }
        else
        {
            final SplitData<T> splitData = ( SplitData<T> ) structureData;
            collectPanes ( splitData.getFirst (), panes );
            collectPanes ( splitData.getLast (), panes );
        }
    }

    public PaneData<T> getPane ( final T document )
    {
        return getPane ( document.getId () );
    }

    public PaneData<T> getPane ( final String documentId )
    {
        for ( final PaneData<T> paneData : getAllPanes () )
        {
            final T document = paneData.get ( documentId );
            if ( document != null )
            {
                return paneData;
            }
        }
        return null;
    }

    public void setSelected ( final int index )
    {
        if ( activePane != null )
        {
            activePane.setSelected ( index );
        }
    }

    public void setSelected ( final DocumentData document )
    {
        setSelected ( document.getId () );
    }

    public void setSelected ( final String id )
    {
        for ( final PaneData<T> paneData : getAllPanes () )
        {
            final T document = paneData.get ( id );
            if ( document != null )
            {
                paneData.setSelected ( document );
            }
        }
    }

    public void openDocument ( final T document )
    {
        if ( activePane != null )
        {
            activePane.add ( document );
        }
    }

    public void closeDocument ( final int index )
    {
        if ( activePane != null )
        {
            activePane.close ( index );
        }
    }

    public void closeDocument ( final String id )
    {
        for ( final PaneData<T> paneData : getAllPanes () )
        {
            paneData.close ( id );
        }
    }

    public void closeDocument ( final T document )
    {
        for ( final PaneData<T> paneData : getAllPanes () )
        {
            paneData.close ( document );
        }
    }

    public PaneData<T> getData ( final WebTabbedPane tabbedPane )
    {
        return ( PaneData<T> ) tabbedPane.getClientProperty ( DATA_KEY );
    }

    public SplitData<T> getData ( final WebSplitPane splitPane )
    {
        return ( SplitData<T> ) splitPane.getClientProperty ( DATA_KEY );
    }
}