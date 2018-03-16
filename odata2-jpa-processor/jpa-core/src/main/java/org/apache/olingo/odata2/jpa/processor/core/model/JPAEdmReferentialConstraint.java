/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 ******************************************************************************/
package org.apache.olingo.odata2.jpa.processor.core.model;

import org.apache.olingo.odata2.api.edm.provider.ReferentialConstraint;
import org.apache.olingo.odata2.jpa.processor.api.access.JPAEdmBuilder;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPAModelException;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmAssociationView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmEntityTypeView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmPropertyView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintRoleView;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintRoleView.RoleType;
import org.apache.olingo.odata2.jpa.processor.api.model.JPAEdmReferentialConstraintView;

public class JPAEdmReferentialConstraint extends JPAEdmBaseViewImpl implements JPAEdmReferentialConstraintView {

  private JPAEdmRefConstraintBuilder builder = null;

  private boolean exists = false;
  private boolean firstBuild = true;

  private JPAEdmAssociationView associationView;
  private JPAEdmPropertyView propertyView;
  private JPAEdmEntityTypeView entityTypeView;

  private ReferentialConstraint referentialConstraint;

  private JPAEdmReferentialConstraintRoleView principalRoleView;
  private JPAEdmReferentialConstraintRoleView dependentRoleView;

  private String relationShipName;

  public JPAEdmReferentialConstraint(final JPAEdmAssociationView associationView,
      final JPAEdmEntityTypeView entityTypeView, final JPAEdmPropertyView propertyView) {
    super(associationView);
    this.associationView = associationView;
    this.propertyView = propertyView;
    this.entityTypeView = entityTypeView;

    relationShipName = associationView.getEdmAssociation().getName();
  }

  @Override
  public JPAEdmBuilder getBuilder() {
    if (builder == null) {
      builder = new JPAEdmRefConstraintBuilder();
    }

    return builder;
  }

  @Override
  public ReferentialConstraint getEdmReferentialConstraint() {
    return referentialConstraint;
  }

  @Override
  public boolean isExists() {
    return exists;
  }

  @Override
  public String getEdmRelationShipName() {
    return relationShipName;
  }

  private class JPAEdmRefConstraintBuilder implements JPAEdmBuilder {
    /*
     * Check if Ref Constraint was already Built. If Ref constraint was
     * already built consistently then do not build referential constraint.
     * 
     * For Ref Constraint to be consistently built Principal and Dependent
     * roles must be consistently built. If Principal or Dependent roles are
     * not consistently built then try building them again.
     * 
     * The Principal and Dependent roles could be have been built
     * inconsistently if the required EDM Entity Types or EDM properties are
     * yet to be built. In such cases rebuilding these roles would make them
     * consistent.
     */
    @Override
    public void build() throws ODataJPAModelException, ODataJPARuntimeException {

      if (firstBuild) {
        firstBuild();
      } else {
        if (exists && !firstBuild && principalRoleView.isConsistent() == false) {
          principalRoleView.getBuilder().build();
        }

        if (exists && !firstBuild && dependentRoleView.isConsistent() == false) {
          dependentRoleView.getBuilder().build();
        }
      }

      if (principalRoleView.isConsistent()) {
        referentialConstraint.setPrincipal(principalRoleView.getEdmReferentialConstraintRole());
      }

      if (dependentRoleView.isConsistent()) {
        referentialConstraint.setDependent(dependentRoleView.getEdmReferentialConstraintRole());
      }

      exists = principalRoleView.isExists() && dependentRoleView.isExists();

      isConsistent = principalRoleView.isConsistent() && dependentRoleView.isConsistent();

    }

    private void firstBuild() throws ODataJPAModelException, ODataJPARuntimeException {
      firstBuild = false;
      if (principalRoleView == null && dependentRoleView == null) {

        principalRoleView =
            new JPAEdmReferentialConstraintRole(RoleType.PRINCIPAL, entityTypeView, propertyView, associationView);
        principalRoleView.getBuilder().build();

        dependentRoleView =
            new JPAEdmReferentialConstraintRole(RoleType.DEPENDENT, entityTypeView, propertyView, associationView);
        dependentRoleView.getBuilder().build();

        if (referentialConstraint == null) {
          referentialConstraint = new ReferentialConstraint();
        }
      }

    }
  }

}
