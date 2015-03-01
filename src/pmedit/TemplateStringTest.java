package pmedit;
import static org.junit.Assert.*;

import org.junit.Test;


public class TemplateStringTest {

	@Test
	public void testProcess() {
		MetadataInfo md = new MetadataInfo();
		md.set("basic.title", "basic_title_1");
		md.set("basic.keywords", "basic_keywords_2_1,basic_keywords_2_2,basic_keywords_2_3");
		assertEquals("",new TemplateString("{nonexistent}").process(md) );
		assertEquals("{{{{",new TemplateString("{{{{").process(md) );
		assertEquals("{{",new TemplateString("{{}{{").process(md) );
		assertEquals("basic_title_1",new TemplateString("{basic.title}").process(md) );
		assertEquals("basic_title_1{",new TemplateString("{basic.title}{").process(md) );
		assertEquals("basic_title_1}",new TemplateString("{basic.title}}").process(md) );
		assertEquals("basic_title_1",new TemplateString("{basic.title}{}").process(md) );
		assertEquals("basic_title_1-basic_keywords_2_1,basic_keywords_2_2,basic_keywords_2_3",
				new TemplateString("{basic.title}-{basic.keywords}").process(md) );

		assertEquals("-ba",
				new TemplateString("{basic.title}-{basic.keywords}", 3).process(md) );
		assertEquals("basi-basic_keywords_",
				new TemplateString("{basic.title}-{basic.keywords}", 20).process(md) );
		assertEquals("1234567890",
				new TemplateString("1234567890", 3).process(md) );
		assertEquals("ba1234567890",
				new TemplateString("{basic.title}1234567890", 12).process(md) );
	}

}
