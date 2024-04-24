package joliex.java.generate.type;

import joliex.java.generate.JavaClassBuilder;
import joliex.java.parse.ast.JolieType;
import joliex.java.parse.ast.JolieType.Definition;
import joliex.java.parse.ast.JolieType.Definition.Basic;
import joliex.java.parse.ast.JolieType.Definition.Choice;
import joliex.java.parse.ast.JolieType.Definition.Structure;
import joliex.java.parse.ast.JolieType.Native;

public abstract class TypeClassBuilder extends JavaClassBuilder {

    protected final String className;
    protected final String typesPackage;

    protected TypeClassBuilder( String className, String typesPackage ) {
        this.className = className;
        this.typesPackage = typesPackage;
    }

    public String className() { return className; }

    public void appendHeader() {
        builder.append( "package " ).append( typesPackage ).append( ";" )
            .newline()
            .newlineAppend( "import jolie.runtime.Value;" )
            .newlineAppend( "import jolie.runtime.ValueVector;" )
            .newlineAppend( "import jolie.runtime.ByteArray;" )
            .newlineAppend( "import jolie.runtime.typing.TypeCheckingException;" )
            .newlineAppend( "import jolie.runtime.embedding.java.JolieValue;" )
            .newlineAppend( "import jolie.runtime.embedding.java.JolieNative;" )
            .newlineAppend( "import jolie.runtime.embedding.java.JolieNative.*;" )
            .newlineAppend( "import jolie.runtime.embedding.java.TypedStructure;" )
            .newlineAppend( "import jolie.runtime.embedding.java.UntypedStructure;" )
            .newlineAppend( "import jolie.runtime.embedding.java.TypeValidationException;" )
            .newlineAppend( "import jolie.runtime.embedding.java.util.*;" )
            .newline()
            .newlineAppend( "import java.util.Arrays;" )
            .newlineAppend( "import java.util.Map;" )
            .newlineAppend( "import java.util.SequencedCollection;" )
            .newlineAppend( "import java.util.List;" )
            .newlineAppend( "import java.util.Optional;" )
            .newlineAppend( "import java.util.Objects;" )
            .newlineAppend( "import java.util.Set;" )
            .newlineAppend( "import java.util.function.Function;" );
    }

    public void appendDefinition() { appendDefinition( false ); }

    public void appendDefinition( boolean isInnerClass ) {
        appendDocumentation();
        appendSignature( isInnerClass );
        builder.body( this::appendBody );
    }

    protected void appendDocumentation() {
		builder.newline().commentBlock( () -> {
			appendDescriptionDocumentation();
			builder.codeBlock( this::appendDefinitionDocumentation );
			appendSeeDocumentation();
		} );
	}

    protected abstract void appendDescriptionDocumentation();
    protected abstract void appendDefinitionDocumentation();
    protected abstract void appendSeeDocumentation();
    protected abstract void appendSignature( boolean isInnerClass );
    protected abstract void appendBody();

    public static TypeClassBuilder create( JolieType type, String typesPackage ) {
        return switch ( type ) {
            case Basic.Inline b -> new BasicClassBuilder( b, typesPackage );
            case Structure.Inline.Typed s -> new TypedStructureClassBuilder( s, typesPackage );
            case Structure.Inline.Untyped s -> new UntypedStructureClassBuilder( s, typesPackage );
            case Choice.Inline c -> new ChoiceClassBuilder( c, typesPackage );
            default -> null;
        };
    }

    protected String qualifiedName( Definition d ) {
        return d.isLink() ? typesPackage + "." + d.name() : d.name(); 
    }

    protected String typeName( JolieType t ) {
        return switch( storedType( t ) ) {
            case Native n -> n == Native.VOID ? n.wrapperName() : n.valueName();
            case Definition d -> qualifiedName( d );
        };
    }

    /*
     * Convenience method to handle the fact that Basic.Inline and Native are stored in the same way.
     */
    protected static JolieType storedType( JolieType t ) {
        return t instanceof Basic.Inline b ? b.nativeType() : t;
    }
}
