/**
 */
package org.key_project.stubby.model.dependencymodel;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Type Variable</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.key_project.stubby.model.dependencymodel.TypeVariable#getName <em>Name</em>}</li>
 *   <li>{@link org.key_project.stubby.model.dependencymodel.TypeVariable#getType <em>Type</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.key_project.stubby.model.dependencymodel.DependencymodelPackage#getTypeVariable()
 * @model
 * @generated
 */
public interface TypeVariable extends EObject {
   /**
    * Returns the value of the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Name</em>' attribute isn't clear,
    * there really should be more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * @return the value of the '<em>Name</em>' attribute.
    * @see #setName(String)
    * @see org.key_project.stubby.model.dependencymodel.DependencymodelPackage#getTypeVariable_Name()
    * @model
    * @generated
    */
   String getName();

   /**
    * Sets the value of the '{@link org.key_project.stubby.model.dependencymodel.TypeVariable#getName <em>Name</em>}' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @param value the new value of the '<em>Name</em>' attribute.
    * @see #getName()
    * @generated
    */
   void setName(String value);

   /**
    * Returns the value of the '<em><b>Type</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <p>
    * If the meaning of the '<em>Type</em>' containment reference isn't clear,
    * there really should be more of a description here...
    * </p>
    * <!-- end-user-doc -->
    * @return the value of the '<em>Type</em>' containment reference.
    * @see #setType(TypeUsage)
    * @see org.key_project.stubby.model.dependencymodel.DependencymodelPackage#getTypeVariable_Type()
    * @model containment="true"
    * @generated
    */
   TypeUsage getType();

   /**
    * Sets the value of the '{@link org.key_project.stubby.model.dependencymodel.TypeVariable#getType <em>Type</em>}' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @param value the new value of the '<em>Type</em>' containment reference.
    * @see #getType()
    * @generated
    */
   void setType(TypeUsage value);

} // TypeVariable
