package joliex.util.types;

import jolie.runtime.Value;
import jolie.runtime.ValueVector;
import jolie.runtime.ByteArray;
import jolie.runtime.typing.TypeCheckingException;
import jolie.runtime.embedding.java.JolieValue;
import jolie.runtime.embedding.java.JolieNative;
import jolie.runtime.embedding.java.JolieNative.*;
import jolie.runtime.embedding.java.TypedStructure;
import jolie.runtime.embedding.java.UntypedStructure;
import jolie.runtime.embedding.java.TypeValidationException;
import jolie.runtime.embedding.java.util.*;

import java.util.Arrays;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

/**
 * this class is a {@link TypedStructure} which can be described as follows:
 * <pre>
 * 
 * contentValue: {@link String}
     * suffix: {@link String}
 * </pre>
 * 
 * @see JolieValue
 * @see JolieNative
 */
public final class EndsWithRequest extends TypedStructure {
    
    private static final Set<String> FIELD_KEYS = fieldKeys( EndsWithRequest.class );
    
    private final String contentValue;
    @JolieName("suffix")
    private final String suffix;
    
    public EndsWithRequest( String contentValue, String suffix ) {
        this.contentValue = ValueManager.validated( "contentValue", contentValue );
        this.suffix = ValueManager.validated( "suffix", suffix );
    }
    
    public String contentValue() { return contentValue; }
    public String suffix() { return suffix; }
    
    public JolieString content() { return new JolieString( contentValue ); }
    
    public static EndsWithRequest from( JolieValue j ) {
        return new EndsWithRequest(
            JolieString.from( j ).value(),
            ValueManager.fieldFrom( j.getFirstChild( "suffix" ), c -> c.content() instanceof JolieString content ? content.value() : null )
        );
    }
    
    public static EndsWithRequest fromValue( Value v ) throws TypeCheckingException {
        ValueManager.requireChildren( v, FIELD_KEYS );
        return new EndsWithRequest(
            JolieString.contentFromValue( v ),
            ValueManager.singleFieldFrom( v, "suffix", JolieString::fieldFromValue )
        );
    }
    
    public static Value toValue( EndsWithRequest t ) {
        final Value v = Value.create( t.contentValue() );
        
        v.getFirstChild( "suffix" ).setValue( t.suffix() );
        
        return v;
    }
}