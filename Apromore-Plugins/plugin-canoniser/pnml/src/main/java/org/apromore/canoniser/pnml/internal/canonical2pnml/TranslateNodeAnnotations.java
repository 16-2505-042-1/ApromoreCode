/*
 * Copyright © 2009-2017 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package org.apromore.canoniser.pnml.internal.canonical2pnml;

import org.apromore.anf.AnnotationType;
import org.apromore.anf.GraphicsType;
import org.apromore.anf.SizeType;
import org.apromore.pnml.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;

public class TranslateNodeAnnotations {

    DataHandler data;

    public void setValue(DataHandler data) {
        this.data = data;
    }

    public void translate(AnnotationType annotation, String cid) {
        GraphicsType cGraphInfo = (GraphicsType) annotation;
        GraphicsNodeType graphics = new GraphicsNodeType();
        GraphicsArcType lines = new GraphicsArcType();
        AnnotationGraphisType annograph = new AnnotationGraphisType();
        org.apromore.pnml.PositionType pos1 = new org.apromore.pnml.PositionType();

        if (cGraphInfo.getFill() != null) {
            Fill fill = new Fill();
            fill.setColor(cGraphInfo.getFill().getColor());
            fill.setGradientColor(cGraphInfo.getFill().getGradientColor());
            fill.setGradientRotation(cGraphInfo.getFill().getGradientRotation());
            fill.setImages(cGraphInfo.getFill().getImage());
            graphics.setFill(fill);
        }

        if (cGraphInfo.getFont() != null) {
            Font font = new Font();
            font.setDecoration(cGraphInfo.getFont().getDecoration());
            font.setFamily(cGraphInfo.getFont().getFamily());
            font.setAlign(cGraphInfo.getFont().getHorizontalAlign());
            font.setRotation(cGraphInfo.getFont().getRotation());
            font.setSize(String.valueOf(cGraphInfo.getFont().getSize()));
            font.setStyle(cGraphInfo.getFont().getStyle());
            font.setWeight(cGraphInfo.getFont().getWeight());
            pos1.setX(cGraphInfo.getFont().getXPosition());
            pos1.setY(cGraphInfo.getFont().getYPosition());
        }

        if (cGraphInfo.getLine() != null) {
            Line line = new Line();
            line.setColor(cGraphInfo.getLine().getColor());
            line.setShape(cGraphInfo.getLine().getShape());
            line.setStyle(cGraphInfo.getLine().getStyle());
            line.setWidth(cGraphInfo.getLine().getWidth());
            lines.setLine(line);
        }

        org.apromore.pnml.PositionType pos = new org.apromore.pnml.PositionType();
        DimensionType dim = new DimensionType();
        
        // Always use standard dimension of 40 for Tool WoPeD
        SizeType size = ((GraphicsType) annotation).getSize();
        dim.setX(BigDecimal.valueOf(Long.valueOf(40)));
        dim.setY(BigDecimal.valueOf(Long.valueOf(40)));
        
        /*----------------------------------------------------
         * Old generic (tool-independent) size coding
        
        if (cGraphInfo.getSize() != null) {
            if (data.getInitialType().equals("PNML")) {
            	if (cGraphInfo != null && cGraphInfo.getSize() != null) {
                dim.setX(cGraphInfo.getSize().getWidth());
                dim.setY(cGraphInfo.getSize().getHeight());
            	} 
            }
        }
        *----------------------------------------------------
        */

        assert cGraphInfo != null;
        if (cGraphInfo.getPosition() != null && cGraphInfo.getPosition().size() > 0) {
        //Neuer Code (MK&JR)
            try{
                BigDecimal dimy = ((GraphicsType) annotation).getSize().getHeight();
                dimy = dimy.round(new MathContext(3, RoundingMode.HALF_UP));
                dimy = dimy.subtract(BigDecimal.valueOf(40));
                dimy = dimy.divide(BigDecimal.valueOf(2));
                dimy = dimy.add(cGraphInfo.getPosition().get(0).getY());
                dimy = dimy.round(new MathContext(3, RoundingMode.HALF_UP));

                pos.setX(cGraphInfo.getPosition().get(0).getX());
                //pos.setY(cGraphInfo.getPosition().get(0).getY());
                pos.setY(dimy);
            } catch (Exception e) {
                /*Do nothing*/
            }

        }
        //
        graphics.setPosition(pos);
        graphics.setDimension(dim);

        Object obj = data.get_pnmlRefMap_value(data.get_id_map_value(cid));
        if (obj instanceof PlaceType) {
            NodeNameType nnt = new NodeNameType();

            if (((PlaceType) obj).getName() != null) {
                if (pos1.getX() != null && pos1.getY() != null) {
                    annograph.getOffsetAndFillAndLine().add(pos1);
                    nnt.setGraphics(annograph);
                }
                nnt.setText(((PlaceType) obj).getName().getText());
                ((PlaceType) obj).setName(nnt);
            }
            ((PlaceType) obj).setGraphics(graphics);
        } else if (obj instanceof TransitionType) {
            NodeNameType nnt = new NodeNameType();
            if (((TransitionType) obj).getName() != null) {
                if (pos1.getX() != null && pos1.getY() != null) {
                    annograph.getOffsetAndFillAndLine().add(pos1);
                    nnt.setGraphics(annograph);
                }
                nnt.setText(((TransitionType) obj).getName().getText());
                ((TransitionType) obj).setName(nnt);
            }
            
            if(((TransitionType) obj).getGraphics().getPosition().isInsertedNode()){
                graphics.getPosition().setInsertedNode(true);
            }
            ((TransitionType) obj).setGraphics(graphics);

            //Neuer Code (MK&JR)
            try {
                ((TransitionType) obj).setName(nnt);
            } catch (Exception e){

            }
            //

            if (data.get_triggermap()!=null&&data.get_triggermap().containsKey(((TransitionType) obj).getName().getText())) {
                TriggerType tt = (data.get_triggermap_value(((TransitionType) obj).getName().getText()));
                PositionType pt = new PositionType();
                pt.setX((((TransitionType) obj).getGraphics().getPosition().getX().add(BigDecimal.valueOf(Long.valueOf(10)))));
                pt.setY((((TransitionType) obj).getGraphics().getPosition().getY().subtract(BigDecimal.valueOf(Long.valueOf(20)))));
                tt.getGraphics().setPosition(pt);
            }
            
            if (data.get_resourcepositionmap()!=null&&data.get_resourcepositionmap().containsKey(((TransitionType) obj).getName().getText())) {
                TransitionResourceType tres = (data.get_resourcepositionmap_value(((TransitionType) obj).getName().getText()));
                PositionType pt = new PositionType();
                pt.setX((((TransitionType) obj).getGraphics().getPosition().getX().subtract(BigDecimal.valueOf(Long.valueOf(10)))));
                pt.setY((((TransitionType) obj).getGraphics().getPosition().getY().subtract(BigDecimal.valueOf(Long.valueOf(47)))));
                tres.getGraphics().setPosition(pt);
            }

        } else if (obj instanceof ArcType) {
            if (cGraphInfo.getPosition() != null && cGraphInfo.getPosition().size() > 0)
                for (int i = 0; i < cGraphInfo.getPosition().size(); i++) {
                    org.apromore.pnml.PositionType pos2 = new org.apromore.pnml.PositionType();

                    pos2.setX(cGraphInfo.getPosition().get(i).getX());
                    pos2.setY(cGraphInfo.getPosition().get(i).getY());

                    lines.getPosition().add(pos2);
                }
            ((ArcType) obj).setGraphics(lines);
        }
    }

}
