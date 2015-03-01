package pmedit;

import static org.junit.Assert.*;

import org.junit.Test;

public class MetadataInfoTest {

	@Test
	public void testYAML() {
		MetadataInfo md  = MetadataInfo.getSampleMetadata();
		String yaml = md.toYAML();
		MetadataInfo md2 = new MetadataInfo();
		md2.fromYAML(yaml);
		assertEquals(md.asFlatMap(), md2.asFlatMap());
	}


}
