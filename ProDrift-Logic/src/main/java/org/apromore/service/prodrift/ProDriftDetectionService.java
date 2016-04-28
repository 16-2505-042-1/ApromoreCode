/*
 * Copyright © 2009-2016 The Apromore Initiative.
 *
 * This file is part of "Apromore".
 *
 * "Apromore" is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version.
 *
 * "Apromore" is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program.
 * If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>.
 */

package org.apromore.service.prodrift;

import ee.ut.eventstr.model.ProDriftDetectionResult;

/**
 * Interface for the Process Drift Detection Service. Defines all the methods that will do the majority of the work for
 * the Apromore application.
 *
 * @author barca
 */
public interface ProDriftDetectionService {

    /**
     * Detect drifts in the log
     * @param logByteArray the log
     * @param winSize the window size
     * @param fWinorAwin Fixed window size or Adaptive window size("FWIN" or "ADWIN")
     * @param logFileName log's name
     * @return the ProDriftDetectionResult from the WebService
     * @throws ProDriftDetectionException if the merge failed
     */
    ProDriftDetectionResult proDriftDetector(byte[] logByteArray, int winSize, String fWinorAwin, String logFileName) throws ProDriftDetectionException;


}
