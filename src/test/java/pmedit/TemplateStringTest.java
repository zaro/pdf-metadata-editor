package pmedit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;



class TemplateStringTest {

	@Test
    void testProcess() {
		MetadataInfo md = new MetadataInfo();
		md.set("doc.title", "basic_title_1");
		md.set("doc.keywords", "basic_keywords_2_1,basic_keywords_2_2,basic_keywords_2_3");
		Assertions.assertEquals("", new TemplateString("{nonexistent}").process(md));
		Assertions.assertEquals("{{{{", new TemplateString("{{{{").process(md));
		Assertions.assertEquals("{{", new TemplateString("{{}{{").process(md));
		Assertions.assertEquals("basic_title_1", new TemplateString("{doc.title}").process(md));
		Assertions.assertEquals("basic_title_1{", new TemplateString("{doc.title}{").process(md));
		Assertions.assertEquals("basic_title_1}", new TemplateString("{doc.title}}").process(md));
		Assertions.assertEquals("basic_title_1", new TemplateString("{doc.title}{}").process(md));
		Assertions.assertEquals("basic_title_1-basic_keywords_2_1,basic_keywords_2_2,basic_keywords_2_3", new TemplateString("{doc.title}-{doc.keywords}").process(md));

		Assertions.assertEquals("-ba", new TemplateString("{doc.title}-{doc.keywords}", 3).process(md));
		Assertions.assertEquals("basi-basic_keywords_", new TemplateString("{doc.title}-{doc.keywords}", 20).process(md));
		Assertions.assertEquals("1234567890", new TemplateString("1234567890", 3).process(md));
		Assertions.assertEquals("ba1234567890", new TemplateString("{doc.title}1234567890", 12).process(md));
	}

}
