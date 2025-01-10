package pmedit;

import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;

import javax.xml.transform.TransformerException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MetadataInfoUtils {
    public static XMPMetadata loadXMPMetadata(InputStream stream) throws XmpParsingException {
        DomXmpParser xmpParser = new DomXmpParser();
        xmpParser.setStrictParsing(false);
        return xmpParser.parse(stream);
    }

    public static byte[] serializeXMPMetadata(XMPMetadata xmpNew) throws TransformerException {
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.serialize(xmpNew, outputStream, true);
        return outputStream.toByteArray();

    }
}
