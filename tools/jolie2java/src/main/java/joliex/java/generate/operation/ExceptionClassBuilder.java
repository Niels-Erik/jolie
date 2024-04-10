package joliex.java.generate.operation;

import java.util.Optional;

import joliex.java.generate.JavaClassBuilder;
import joliex.java.parse.ast.JolieType.Definition;
import joliex.java.parse.ast.JolieType.Native;
import joliex.java.parse.ast.JolieType.Definition.Basic;
import joliex.java.parse.ast.JolieType.Definition.Choice;
import joliex.java.parse.ast.JolieType.Definition.Structure;
import joliex.java.parse.ast.JolieType.Definition.Structure.Undefined;
import joliex.java.parse.ast.JolieOperation.RequestResponse.Fault;

public abstract class ExceptionClassBuilder extends JavaClassBuilder {

    private final String className;
    private final String packageName;
    private final String typesFolder;
    private final String faultsFolder;

    protected final String faultName;

    protected ExceptionClassBuilder( String className, String packageName, String typesFolder, String faultsFolder, String faultName ) {
        this.className = className;
        this.packageName = packageName;
        this.typesFolder = typesFolder;
        this.faultsFolder = faultsFolder;
        this.faultName = faultName;
    }

    public String className() { return className; }

    public void appendHeader() { 
        builder.append( "package " ).append( packageName ).append( "." ).append( faultsFolder ).append( ";" )
            .newline()
            .newlineAppend( "import jolie.runtime.FaultException;" );
    }

    public void appendDefinition() {
        builder.newNewlineAppend( "public class " ).append( className() ).append( " extends FaultException" )
            .body( this::appendDefinitionBody );
    }

    protected abstract void appendDefinitionBody();

    public static ExceptionClassBuilder create( Fault fault, String packageName, String typesFolder, String faultsFolder ) {
        return switch ( fault.type() ) {
            case Native n -> n == Native.VOID
                ? new VoidExceptionBuilder( fault.className(), packageName, typesFolder, faultsFolder, fault.name() )
                : new NativeExceptionBuilder( fault.className(), packageName, typesFolder, faultsFolder, fault.name(), n );
            case Definition d -> new DefinitionExceptionBuilder( fault.className(), packageName, typesFolder, faultsFolder, fault.name(), d );
        };
    }

    public static class VoidExceptionBuilder extends ExceptionClassBuilder {

        public VoidExceptionBuilder( String className, String packageName, String typesFolder, String faultsFolder, String faultName ) {
            super( className, packageName, typesFolder, faultsFolder, faultName );
        }

        protected void appendDefinitionBody() {
            builder.newNewlineAppend( "public " ).append( className() ).append( "() { super( \"" ).append( faultName ).append( "\" ); }" );
        }
    }

    public static class NativeExceptionBuilder extends ExceptionClassBuilder {

        private final Native type;

        public NativeExceptionBuilder( String className, String packageName, String typesFolder, String faultsFolder, String faultName, Native type ) {
            super( className, packageName, typesFolder, faultsFolder, faultName );
            this.type = type;
        }

        public void appendHeader() {
            super.appendHeader();

            if ( type == Native.ANY )
                builder.newlineAppend( "import jolie.runtime.ByteArray;" )
                    .newline()
                    .newlineAppend( "import joliex.java.embedding.JolieNative;" );
            else {
                builder.newlineAppend( "import jolie.runtime.Value;" );

                if ( type == Native.RAW )
                    builder.newlineAppend( "import jolie.runtime.ByteArray;" );
            }
        }

        protected void appendDefinitionBody() {
            builder.newline()
                .newlineAppend( "private final " ).append( type.valueName() ).append( " fault;" )
                .newline()
                .newlineAppend( "public " ).append( type.valueName() ).append( " fault() { return fault; }" )
                .newline()
                .newlineAppend( "public " ).append( className() ).append( "( " ).append( type.valueName() ).append( " fault )" )
                .body( () -> builder
                    .newlineAppend( "super( \"" ).append( faultName ).append( "\", " ).append( type == Native.ANY ? "fault.jolieRepr()" : "Value.create( fault )" ).append( " );" )
                    .newlineAppend( "this.fault = fault;" )
                );

            if ( type == Native.ANY ) {
                type.valueNames().forEach( 
                    vn -> builder.newlineAppend( "public " ).append( className() ).append( "( " ).append( vn ).append( " faultValue ) { this( JolieNative.create( faultValue ) ); }" ) 
                );
                builder.newlineAppend( "public " ).append( className() ).append( "() { this( JolieNative.create() ); }" ); 
            }
        }
    }

    public static class DefinitionExceptionBuilder extends ExceptionClassBuilder {

        private final Definition definition;

        public DefinitionExceptionBuilder( String className, String packageName, String typesFolder, String faultsFolder, String faultName, Definition definition ) {
            super( className, packageName, typesFolder, faultsFolder, faultName );
            this.definition = definition;
        }

        public void appendHeader() {
            super.appendHeader();

            builder
                .newlineAppend( "import joliex.java.embedding.*;" )
                .newline()
                .newlineAppend( "import java.util.function.Function;" );

            if ( !(definition instanceof Undefined) )
                builder.newNewlineAppend( "import " ).append( super.packageName ).append( "." ).append( super.typesFolder ).append( ".*;" );
        }

        protected void appendDefinitionBody() {
            builder.newline()
                .newlineAppend( "private final " ).append( definition.name() ).append( " fault;" )
                .newline()
                .newlineAppend( "public " ).append( definition.name() ).append( " fault() { return fault; }" )
                .newline()
                .newlineAppend( "public " ).append( className() ).append( "( " ).append( definition.name() ).append( " fault )" )
                .body( () -> builder
                    .newlineAppend( "super( \"" ).append( faultName ).append( "\", JolieValue.toValue( fault ) );" )
                    .newlineAppend( "this.fault = fault;" )
                );

            switch ( definition ) {
                case Basic b -> appendExtraConstructor( b.nativeType().valueName(), "faultValue", b.name(), "" );
                case Choice c -> appendOptionMethods( c );
                case Structure s -> appendBuilderMethods( s.name() );
            }

            if ( !(definition instanceof Undefined) )
                builder.newNewlineAppend( "public static " ).append( className() ).append( " createFrom( JolieValue t ) { return new " ).append( className() ).append( "( " ).append( definition.name() ).append( ".createFrom( t ) ); }" );
        }

        private void appendOptionMethods( Choice choice ) {
            choice.numberedOptions()
                .mapValues( t -> switch( t ) { 
                    case Native n -> n == Native.VOID ? null : n.valueName(); 
                    case Structure.Inline s -> choice.name() + "." + s.name(); 
                    case Definition d -> d.name(); 
                } )
                .distinctValues()
                .forKeyValue( (i,n) -> appendExtraConstructor( n, "faultOption", choice.name(), String.valueOf( i ) ) );
            
            choice.numberedOptions()
                .mapToValuePartial( (i,t) -> Optional.ofNullable( switch ( t ) { 
                    case Structure.Inline si -> choice.name() + "." + si.name();
                    case Structure.Link sl -> sl.name();
                    default -> null;
                } ) )
                .forKeyValue( (i,n) -> appendBuilderMethods( n, String.valueOf( i ) ) );
        }

        private void appendExtraConstructor( String paramType, String paramName, String wrapperClass, String createPostfix ) {
            final Optional<String> p = Optional.ofNullable( paramType );
            builder.newlineAppend( "public " ).append( className() ).append( p.map( t -> "( " + t + " " + paramName + " )" ).orElse( "()" ) ).append( " { this( " ).append( wrapperClass ).append( ".create" ).append( createPostfix ).append( p.map( t -> "( " + paramName + " )" ).orElse( "()" ) ).append( " ); }" );
        }

        private void appendBuilderMethods( String builderClassName ) { appendBuilderMethods( builderClassName, "" ); }
        private void appendBuilderMethods( String builderClassName, String methodPostfix ) {
            builder.newline()
                .newlineAppend( "public static " ).append( className() ).append( " create" ).append( methodPostfix ).append( "( Function<" ).append( builderClassName ).append( ".InlineBuilder, " ).append( builderClassName ).append( "> builder ) { return new " ).append( className() ).append( "( builder.apply( " ).append( builderClassName ).append( ".construct() ) ); }" )
                .newlineAppend( "public static " ).append( className() ).append( " create" ).append( methodPostfix ).append( "From( JolieValue t, Function<" ).append( builderClassName ).append( ".InlineBuilder, " ).append( builderClassName ).append( "> rebuilder ) { return new " ).append( className() ).append( "( rebuilder.apply( " ).append( builderClassName ).append( ".constructFrom( t ) ) ); }" );
        }
    }
}
