package com.alee.managers.style.skin.web;

import com.alee.laf.tooltip.ToolTipPainter;
import com.alee.laf.tooltip.WebToolTipUI;

import javax.swing.*;
import java.awt.*;

/**
 * @author Alexandr Zernov
 */

public class WebTooltipPainter<E extends JComponent, U extends WebToolTipUI> extends WebDecorationPainter<E, U>
        implements ToolTipPainter<E, U>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public void paint ( final Graphics2D g2d, final Rectangle bounds, final E c, final U ui )
    {

    }
}
