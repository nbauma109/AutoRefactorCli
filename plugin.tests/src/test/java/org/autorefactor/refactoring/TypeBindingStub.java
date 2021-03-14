/*
 * AutoRefactor - Eclipse plugin to automatically refactor Java code bases.
 *
 * Copyright (C) 2016 Jean-Noël Rouvignac - initial API and implementation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program under LICENSE-GNUGPL.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution under LICENSE-ECLIPSE, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.autorefactor.refactoring;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

class TypeBindingStub implements ITypeBinding {
	private final String fullyQualifiedName;
	private final IPackageBinding packageBinding;
	private final TypeBindingStub declaringClass;

	TypeBindingStub(String fullyQualifiedName) {
		this.fullyQualifiedName= fullyQualifiedName;
		this.packageBinding= toPackage(fullyQualifiedName);
		this.declaringClass= toDeclaringClass(fullyQualifiedName);
	}

	public IPackageBinding toPackage(String fullyQualifiedName) {
		final String[] names= fullyQualifiedName.split("\\."); //$NON-NLS-1$
		for (int i= 0; i < names.length; i++) {
			if (Character.isUpperCase(names[i].charAt(0))) {
				return new PackageBindingStub(joinAsString(names, i, ".")); //$NON-NLS-1$
			}
		}
		throw new IllegalStateException("Did not expect to get there"); //$NON-NLS-1$
	}

	public TypeBindingStub toDeclaringClass(String fullyQualifiedName) {
		final String[] names= fullyQualifiedName.split("\\."); //$NON-NLS-1$
		int length= names.length;
		if (Character.isUpperCase(names[length - 1].charAt(0)) && Character.isUpperCase(names[length - 2].charAt(0))) {
			return new TypeBindingStub(joinAsString(names, length - 1, ".")); //$NON-NLS-1$
		}

		return null;
	}

	private String joinAsString(String[] names, int limit, String separator) {
		final StringBuilder sb= new StringBuilder();
		sb.append(names[0]);
		for (int i= 1; i < limit; i++) {
			sb.append(separator).append(names[i]);
		}

		return sb.toString();
	}

	/**
	 * Get the annotations.
	 *
	 * @return the annotations.
	 */
	@Override
	public IAnnotationBinding[] getAnnotations() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the Java element.
	 *
	 * @return the Java element.
	 */
	@Override
	public IJavaElement getJavaElement() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the key.
	 *
	 * @return the key.
	 */
	@Override
	public String getKey() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the kind.
	 *
	 * @return the kind.
	 */
	@Override
	public int getKind() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is deprecated.
	 *
	 * @return True if it is deprecated.
	 */
	@Override
	public boolean isDeprecated() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is equal.
	 *
	 * @return True if it is equal.
	 */
	@Override
	public boolean isEqualTo(IBinding arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is recovered.
	 *
	 * @return True if it is recovered.
	 */
	@Override
	public boolean isRecovered() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is synthetic.
	 *
	 * @return True if it is synthetic.
	 */
	@Override
	public boolean isSynthetic() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Create the array type.
	 *
	 * @return the array type.
	 */
	@Override
	public ITypeBinding createArrayType(int arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the binary name.
	 *
	 * @return the binary name.
	 */
	@Override
	public String getBinaryName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the bound.
	 *
	 * @return the bound.
	 */
	@Override
	public ITypeBinding getBound() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the component type.
	 *
	 * @return the component type.
	 */
	@Override
	public ITypeBinding getComponentType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the declared fields.
	 *
	 * @return the declared fields.
	 */
	@Override
	public IVariableBinding[] getDeclaredFields() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the declared methods.
	 *
	 * @return the declared methods.
	 */
	@Override
	public IMethodBinding[] getDeclaredMethods() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the declared modifiers.
	 *
	 * @return the declared modifiers.
	 */
	@Override
	public int getDeclaredModifiers() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the declared types.
	 *
	 * @return the declared types.
	 */
	@Override
	public ITypeBinding[] getDeclaredTypes() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the declared class.
	 *
	 * @return the declared class.
	 */
	@Override
	public ITypeBinding getDeclaringClass() {
		return declaringClass;
	}

	@Override
	public IBinding getDeclaringMember() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the declaring method.
	 *
	 * @return the declaring method.
	 */
	@Override
	public IMethodBinding getDeclaringMethod() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the dimensions.
	 *
	 * @return the dimensions.
	 */
	@Override
	public int getDimensions() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the element type.
	 *
	 * @return the element type.
	 */
	@Override
	public ITypeBinding getElementType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the erasure.
	 *
	 * @return the erasure.
	 */
	@Override
	public ITypeBinding getErasure() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IMethodBinding getFunctionalInterfaceMethod() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the generic type of wildcard type.
	 *
	 * @return the generic type of wildcard type.
	 */
	@Override
	public ITypeBinding getGenericTypeOfWildcardType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the interfaces.
	 *
	 * @return the interfaces.
	 */
	@Override
	public ITypeBinding[] getInterfaces() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the modifiers.
	 *
	 * @return the modifiers.
	 */
	@Override
	public int getModifiers() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the name.
	 *
	 * @return the name.
	 */
	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the package.
	 *
	 * @return the package.
	 */
	@Override
	public IPackageBinding getPackage() {
		return packageBinding;
	}

	/**
	 * Get the fully qualified name.
	 *
	 * @return the fully qualified name.
	 */
	@Override
	public String getQualifiedName() {
		return fullyQualifiedName;
	}

	/**
	 * Get the rank.
	 *
	 * @return the rank.
	 */
	@Override
	public int getRank() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the superclass.
	 *
	 * @return the superclass.
	 */
	@Override
	public ITypeBinding getSuperclass() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IAnnotationBinding[] getTypeAnnotations() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the type arguments.
	 *
	 * @return the type arguments.
	 */
	@Override
	public ITypeBinding[] getTypeArguments() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the type bounds.
	 *
	 * @return the type bounds.
	 */
	@Override
	public ITypeBinding[] getTypeBounds() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the type declaration.
	 *
	 * @return the type declaration.
	 */
	@Override
	public ITypeBinding getTypeDeclaration() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the fully qualified name.
	 *
	 * @return the fully qualified name.
	 */
	@Override
	public ITypeBinding[] getTypeParameters() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get the wildcard.
	 *
	 * @return the wildcard.
	 */
	@Override
	public ITypeBinding getWildcard() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is an annotation.
	 *
	 * @return True if it is an annotation.
	 */
	@Override
	public boolean isAnnotation() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is anonymous.
	 *
	 * @return True if it is anonymous.
	 */
	@Override
	public boolean isAnonymous() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is an array.
	 *
	 * @return True if it is an array.
	 */
	@Override
	public boolean isArray() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is assignment compatible.
	 *
	 * @return True if it is assignment compatible.
	 */
	@Override
	public boolean isAssignmentCompatible(ITypeBinding arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is capture.
	 *
	 * @return True if it is capture.
	 */
	@Override
	public boolean isCapture() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is cast compatible.
	 *
	 * @return True if it is cast compatible.
	 */
	@Override
	public boolean isCastCompatible(ITypeBinding arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is class.
	 *
	 * @return True if it is class.
	 */
	@Override
	public boolean isClass() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is enum.
	 *
	 * @return True if it is enum.
	 */
	@Override
	public boolean isEnum() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is from source.
	 *
	 * @return True if it is from source.
	 */
	@Override
	public boolean isFromSource() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is generic type.
	 *
	 * @return True if it is generic type.
	 */
	@Override
	public boolean isGenericType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is interface.
	 *
	 * @return True if it is interface.
	 */
	@Override
	public boolean isInterface() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is local.
	 *
	 * @return True if it is local.
	 */
	@Override
	public boolean isLocal() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is member.
	 *
	 * @return True if it is member.
	 */
	@Override
	public boolean isMember() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is nested.
	 *
	 * @return True if it is nested.
	 */
	@Override
	public boolean isNested() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is null type.
	 *
	 * @return True if it is null type.
	 */
	@Override
	public boolean isNullType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is parameterized type.
	 *
	 * @return True if it is parameterized type.
	 */
	@Override
	public boolean isParameterizedType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is primitive.
	 *
	 * @return True if it is primitive.
	 */
	@Override
	public boolean isPrimitive() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is raw type.
	 *
	 * @return True if it is raw type.
	 */
	@Override
	public boolean isRawType() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is sub type compatible.
	 *
	 * @return True if it is sub type compatible.
	 */
	@Override
	public boolean isSubTypeCompatible(ITypeBinding arg0) {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is top level.
	 *
	 * @return True if it is top level.
	 */
	@Override
	public boolean isTopLevel() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is type variable.
	 *
	 * @return True if it is type variable.
	 */
	@Override
	public boolean isTypeVariable() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is upperbound.
	 *
	 * @return True if it is upperbound.
	 */
	@Override
	public boolean isUpperbound() {
		throw new UnsupportedOperationException();
	}

	/**
	 * True if it is unnamed.
	 *
	 * @return True if it is unnamed.
	 */
	@Override
	public boolean isWildcardType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIntersectionType() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String toString() {
		return fullyQualifiedName;
	}

	@Override
	public boolean isRecord() {
		return false;
	}
}
