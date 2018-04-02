package org.autorefactor.matcher;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;

public final class AstMatcherUtil {

	// TODOs: optimize indent, trim strings (with ellipsis?)
	    public static void dumpAst(ASTNode n) {
	        n.accept(new ASTVisitor() {
	            private int indent;
	
	            private void printIndent() {
	                if (indent > 0) {
	                    System.out.printf("%" + indent + "s", "");
	                }
	            }
	
	            // TODO: improve for InfixExpression
	            @Override
	            public void preVisit(ASTNode node) {
	                //super.preVisit(node);
	                StructuralPropertyDescriptor loc = node.getLocationInParent();
	                int index = -1;
	                if (loc != null && loc.isChildListProperty()) {
	                    List<?> l = (List<?>)node.getParent().getStructuralProperty(loc);
	                    index = l.indexOf(node);
	                }
	                String locId = loc != null ? loc.getId() + ": " : "";
	                String name = "";
	                String fqn = "";
	                {
	                    if (node instanceof SimpleName) {
	                        SimpleName n = (SimpleName) node;
	                        //IBinding type = n.resolveTypeBinding();
	                        IBinding binding = n.resolveBinding();
	                        //fqn = n.getFullyQualifiedName();
	                        if (binding instanceof IVariableBinding) {
	                            IVariableBinding vb = (IVariableBinding) binding;
	                            fqn = vb.getDeclaringMethod() == null && vb.getDeclaringClass() != null
	                                    ? vb.getDeclaringClass().getQualifiedName() + "." + n.getIdentifier()
	                                    : "";
	                        } else if (binding instanceof IMethodBinding) {
	                        	IMethodBinding mb = (IMethodBinding) binding;
	                        	fqn = mb.getDeclaringClass() != null
	                        			? mb.getDeclaringClass().getQualifiedName() + "." + n.getIdentifier()
	                        			: "";
	                        }
	
	//                        fqn = type != null ?
	//                                fqn + ":" + /*binding.getName()
	//                                  // TODO: convert internal type key to external form
	//                            + ":key=" + */ binding.getClass().getSimpleName() + ":" + binding.getKey()
	//                            //+ ":je=" + binding.getJavaElement()
	//                            //+ ":" + type.getName()
	//                            : "";
	
	                    } else if (node instanceof SimpleName || node.getClass().getName().endsWith("Literal")) {
	                        name = node.toString();
	                    } else {
	                        try {
	                            Field nameProperty = node.getClass().getField("NAME_PROPERTY");
	                            Object nameNode = node.getStructuralProperty((StructuralPropertyDescriptor)nameProperty.get(null));
	                            name = nameNode != null ? String.valueOf(nameNode) : "";
	                        } catch (Exception ignore) {}
	                    }
	                }
	
	                // TODO: add position and name + string rep in '', string rep to non index, too
	                // TODO: compile pattern once
	                String rawAsString = String.valueOf(node).replaceFirst("(?s)\n.*", "");
	                rawAsString = rawAsString.length() > 60 ? rawAsString.substring(0,  40) : rawAsString;
	                String nodeAsString = rawAsString.isEmpty() ? "" : (" '" + rawAsString + "'");
	                //name = name.length() > 40 ? name.substring(0,  60) : "";
	                // TODO: think about qualified names
	                name = "";
	                name = name.isEmpty() ? "" : (" '" + name + "'");
	                fqn = fqn.isEmpty() ? "" : (" '" + fqn + "'");
	                String pos = InternalMatcherUtil.location(node);
	                if (index == -1) {
	                    if (locId.isEmpty()) {
	                    printIndent();
	                    System.out.printf("%s%s %08x%s%s%s%n",
	                            node.getClass().getSimpleName(),
	                            pos,
	                            System.identityHashCode(node), name,
	                            fqn,
	                            nodeAsString);
	                    } else {
	                        printIndent();
	                        System.out.printf("%s%n", locId);
	                        printIndent();
	                        System.out.printf("  %s%s %08x%s%s%s%n",
	                                    node.getClass().getSimpleName(),
	                                    pos,
	                                    System.identityHashCode(node), name,
	                                    fqn,
	                                    nodeAsString);
	
	                    }
	                } else {
	                    if (index == 0) {
	                        printIndent();
	                        System.out.printf("%s%n", locId);
	                    }
	                    printIndent();
	                    System.out.printf("- %s%s %08x%s%s%s%n",
	                                node.getClass().getSimpleName(),
	                                pos,
	                                System.identityHashCode(node),
	                                name,
	                                fqn,
	                                nodeAsString);
	                }
	                indent += 4;
	            }
	
				@Override
	            public void postVisit(ASTNode node) {
	                indent -= 4;
	            }
	        });
	    }

}
