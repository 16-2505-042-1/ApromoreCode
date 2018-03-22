/*
 * Copyright © 2009-2018 The Apromore Initiative.
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

package org.apromore.service.dataflow.impl;

// Java 2 Enterprise Edition
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

// Third party packages
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Entity
@Table(name = "topic", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id"}),
    @UniqueConstraint(columnNames = {"name"}),
})
public class Topic {

    private static Logger LOGGER = LoggerFactory.getLogger(Topic.class.getCanonicalName());

    private Integer id;
    private DataflowImpl dataflow;
    private String name;

    // Database field accessors

    /** @return primary key */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    public Integer getId() {
        return id;
    }

    /** @param newId  the new primary key */
    public void setId(final Integer newId) {
        this.id = newId;
    }

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "dataflowId", nullable = false)
    public DataflowImpl getDataflow() {
        return dataflow;
    }

    /** @param newId  the new primary key */
    public void setDataflow(final DataflowImpl newDataflow) {
        this.dataflow = newDataflow;
    }

    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }

    /** @param newId  the new primary key */
    public void setName(final String newName) {
        this.name = newName;
    }
}
