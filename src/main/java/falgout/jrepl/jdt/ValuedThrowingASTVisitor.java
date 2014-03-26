package falgout.jrepl.jdt;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public abstract class ValuedThrowingASTVisitor<R, X extends Throwable> {
    private final Class<X> x;
    
    protected ValuedThrowingASTVisitor(Class<X> x) {
        this.x = x;
    }
    
    public Class<X> getExceptionClass() {
        return x;
    }
    
    public R visit(ASTNode node) throws X {
        try {
            VisitorBridge<R, X> bridge = new VisitorBridge<>(this);
            node.accept(bridge);
            return bridge.getValue();
        } catch (BridgeException e) {
            throw x.cast(e.getCause());
        }
    }
    
    public R visit(AnnotationTypeDeclaration node) throws X {
        return null;
    }
    
    public R visit(AnnotationTypeMemberDeclaration node) throws X {
        return null;
    }
    
    public R visit(AnonymousClassDeclaration node) throws X {
        return null;
    }
    
    public R visit(ArrayAccess node) throws X {
        return null;
    }
    
    public R visit(ArrayCreation node) throws X {
        return null;
    }
    
    public R visit(ArrayInitializer node) throws X {
        return null;
    }
    
    public R visit(ArrayType node) throws X {
        return null;
    }
    
    public R visit(AssertStatement node) throws X {
        return null;
    }
    
    public R visit(Assignment node) throws X {
        return null;
    }
    
    public R visit(Block node) throws X {
        return null;
    }
    
    public R visit(BlockComment node) throws X {
        return null;
    }
    
    public R visit(BooleanLiteral node) throws X {
        return null;
    }
    
    public R visit(BreakStatement node) throws X {
        return null;
    }
    
    public R visit(CastExpression node) throws X {
        return null;
    }
    
    public R visit(CatchClause node) throws X {
        return null;
    }
    
    public R visit(CharacterLiteral node) throws X {
        return null;
    }
    
    public R visit(ClassInstanceCreation node) throws X {
        return null;
    }
    
    public R visit(CompilationUnit node) throws X {
        return null;
    }
    
    public R visit(ConditionalExpression node) throws X {
        return null;
    }
    
    public R visit(ConstructorInvocation node) throws X {
        return null;
    }
    
    public R visit(ContinueStatement node) throws X {
        return null;
    }
    
    public R visit(DoStatement node) throws X {
        return null;
    }
    
    public R visit(EmptyStatement node) throws X {
        return null;
    }
    
    public R visit(EnhancedForStatement node) throws X {
        return null;
    }
    
    public R visit(EnumConstantDeclaration node) throws X {
        return null;
    }
    
    public R visit(EnumDeclaration node) throws X {
        return null;
    }
    
    public R visit(ExpressionStatement node) throws X {
        return null;
    }
    
    public R visit(FieldAccess node) throws X {
        return null;
    }
    
    public R visit(FieldDeclaration node) throws X {
        return null;
    }
    
    public R visit(ForStatement node) throws X {
        return null;
    }
    
    public R visit(IfStatement node) throws X {
        return null;
    }
    
    public R visit(ImportDeclaration node) throws X {
        return null;
    }
    
    public R visit(InfixExpression node) throws X {
        return null;
    }
    
    public R visit(InstanceofExpression node) throws X {
        return null;
    }
    
    public R visit(Initializer node) throws X {
        return null;
    }
    
    public R visit(LabeledStatement node) throws X {
        return null;
    }
    
    public R visit(LineComment node) throws X {
        return null;
    }
    
    public R visit(MarkerAnnotation node) throws X {
        return null;
    }
    
    public R visit(MemberRef node) throws X {
        return null;
    }
    
    public R visit(MemberValuePair node) throws X {
        return null;
    }
    
    public R visit(MethodRef node) throws X {
        return null;
    }
    
    public R visit(MethodRefParameter node) throws X {
        return null;
    }
    
    public R visit(MethodDeclaration node) throws X {
        return null;
    }
    
    public R visit(MethodInvocation node) throws X {
        return null;
    }
    
    public R visit(Modifier node) throws X {
        return null;
    }
    
    public R visit(NormalAnnotation node) throws X {
        return null;
    }
    
    public R visit(NullLiteral node) throws X {
        return null;
    }
    
    public R visit(NumberLiteral node) throws X {
        return null;
    }
    
    public R visit(PackageDeclaration node) throws X {
        return null;
    }
    
    public R visit(ParameterizedType node) throws X {
        return null;
    }
    
    public R visit(ParenthesizedExpression node) throws X {
        return null;
    }
    
    public R visit(PostfixExpression node) throws X {
        return null;
    }
    
    public R visit(PrefixExpression node) throws X {
        return null;
    }
    
    public R visit(PrimitiveType node) throws X {
        return null;
    }
    
    public R visit(QualifiedName node) throws X {
        return null;
    }
    
    public R visit(QualifiedType node) throws X {
        return null;
    }
    
    public R visit(ReturnStatement node) throws X {
        return null;
    }
    
    public R visit(SimpleName node) throws X {
        return null;
    }
    
    public R visit(SimpleType node) throws X {
        return null;
    }
    
    public R visit(SingleMemberAnnotation node) throws X {
        return null;
    }
    
    public R visit(SingleVariableDeclaration node) throws X {
        return null;
    }
    
    public R visit(StringLiteral node) throws X {
        return null;
    }
    
    public R visit(SuperConstructorInvocation node) throws X {
        return null;
    }
    
    public R visit(SuperFieldAccess node) throws X {
        return null;
    }
    
    public R visit(SuperMethodInvocation node) throws X {
        return null;
    }
    
    public R visit(SwitchCase node) throws X {
        return null;
    }
    
    public R visit(SwitchStatement node) throws X {
        return null;
    }
    
    public R visit(SynchronizedStatement node) throws X {
        return null;
    }
    
    public R visit(TagElement node) throws X {
        return null;
    }
    
    public R visit(TextElement node) throws X {
        return null;
    }
    
    public R visit(ThisExpression node) throws X {
        return null;
    }
    
    public R visit(ThrowStatement node) throws X {
        return null;
    }
    
    public R visit(TryStatement node) throws X {
        return null;
    }
    
    public R visit(TypeDeclaration node) throws X {
        return null;
    }
    
    public R visit(TypeDeclarationStatement node) throws X {
        return null;
    }
    
    public R visit(TypeLiteral node) throws X {
        return null;
    }
    
    public R visit(TypeParameter node) throws X {
        return null;
    }
    
    public R visit(UnionType node) throws X {
        return null;
    }
    
    public R visit(VariableDeclarationExpression node) throws X {
        return null;
    }
    
    public R visit(VariableDeclarationStatement node) throws X {
        return null;
    }
    
    public R visit(VariableDeclarationFragment node) throws X {
        return null;
    }
    
    public R visit(WhileStatement node) throws X {
        return null;
    }
    
    public R visit(WildcardType node) throws X {
        return null;
    }
}
