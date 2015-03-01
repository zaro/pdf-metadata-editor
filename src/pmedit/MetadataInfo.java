package pmedit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.function.Function;

import javax.swing.JOptionPane;
import javax.xml.transform.TransformerException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.util.StringUtil;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.DumperOptions.FlowStyle;
import org.yaml.snakeyaml.DumperOptions.ScalarStyle;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.DumperOptions.LineBreak;

public class MetadataInfo {

	public static class Basic {
		public String title;
		public String author;
		public String subject;
		public String keywords;
		public String creator;
		public String producer;
		public Calendar creationDate;
		public Calendar modificationDate;
		public String trapped;
	};

	public static class XmpBasic {
		public String creatorTool;
		public Calendar createDate;
		public Calendar modifyDate;
		public String title;
		public String baseURL;
		public Integer rating;
		public String label;
		public String nickname;
		public List<String> identifiers;
		public List<String> advisories;
		public Calendar metadataDate;
	};

	public static class XmpPdf {
		public String pdfVersion;
		public String keywords;
		public String producer;
	};

	public static class XmpDublinCore {
		public String title;
		public String description;
		public List<String> creators;
		public List<String> contributors;
		public String coverage;
		public List<Calendar> dates;
		public String format;
		public String identifier;
		public List<String> languages;
		public List<String> publishers;
		public List<String> relationships;
		public String rights;
		public String source;
		public List<String> subjects;
		public List<String> types;
	};

	public Basic basic = new Basic();
	public XmpBasic xmpBasic = new XmpBasic();
	public XmpPdf xmpPdf = new XmpPdf();
	public XmpDublinCore xmpDc = new XmpDublinCore();

	public void loadFromPDF(PDDocument document) throws IOException {
		PDDocumentInformation info = document.getDocumentInformation();

		// Basic info
		basic.title = info.getTitle();
		basic.author = info.getAuthor();
		basic.subject = info.getSubject();
		basic.keywords = info.getKeywords();
		basic.creator = info.getCreator();
		basic.producer = info.getProducer();
		basic.creationDate = info.getCreationDate();
		basic.modificationDate = info.getModificationDate();
		basic.trapped = info.getTrapped();

		// Load XMP catalog
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
		if (metadata != null) {
			// XMP Basic
			XMPMetadata xmp = XMPMetadata.load(metadata.createInputStream());
			XMPSchemaBasic bi = xmp.getBasicSchema();
			if (bi != null) {

				xmpBasic.creatorTool = bi.getCreatorTool();
				xmpBasic.createDate = bi.getCreateDate();
				xmpBasic.modifyDate = bi.getModifyDate();
				xmpBasic.title = bi.getTitle();
				xmpBasic.baseURL = bi.getBaseURL();
				xmpBasic.rating = bi.getRating();
				xmpBasic.label = bi.getLabel();
				xmpBasic.nickname = bi.getNickname();
				xmpBasic.identifiers = bi.getIdentifiers();
				xmpBasic.advisories = bi.getAdvisories();
				xmpBasic.metadataDate = bi.getMetadataDate();
			}

			// XMP PDF
			XMPSchemaPDF pi = xmp.getPDFSchema();
			if (pi != null) {
				xmpPdf.pdfVersion = pi.getPDFVersion();
				xmpPdf.keywords = pi.getKeywords();
				xmpPdf.producer = pi.getProducer();
			}

			// XMP Dublin Core
			XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
			if (dc != null) {
				xmpDc.title = dc.getTitle();
				xmpDc.description = dc.getDescription();
				xmpDc.creators = dc.getCreators();
				xmpDc.contributors = dc.getContributors();
				xmpDc.coverage = dc.getCoverage();
				xmpDc.dates = dc.getDates();
				xmpDc.format = dc.getFormat();
				xmpDc.identifier = dc.getIdentifier();
				xmpDc.languages = dc.getLanguages();
				xmpDc.publishers = dc.getPublishers();
				xmpDc.relationships = dc.getRelationships();
				xmpDc.rights = dc.getRights();
				xmpDc.source = dc.getSource();
				xmpDc.subjects = dc.getSubjects();
				xmpDc.types = dc.getTypes();
			}
		}

		//System.err.println("Loaded:");
		//System.err.println(toYAML());

	}

	public void loadFromPDF(File pdfFile) throws FileNotFoundException,
			IOException {
		PDDocument document = null;

		document = PDDocument.load(new FileInputStream(pdfFile));

		loadFromPDF(document);

		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}
	}

	public void saveToPDF(PDDocument document, File pdfFile) throws Exception {

		//System.err.println("Saving:");
		//System.err.println(toYAML());
		// Basic info
		PDDocumentInformation info = document.getDocumentInformation();
		info.setTitle(basic.title);
		info.setAuthor(basic.author);
		info.setSubject(basic.subject);
		info.setKeywords(basic.keywords);
		info.setCreator(basic.creator);
		info.setProducer(basic.producer);
		info.setCreationDate(basic.creationDate);
		info.setModificationDate(basic.modificationDate);
		info.setTrapped(basic.trapped);
		document.setDocumentInformation(info);

		// XMP
		PDDocumentCatalog catalog = document.getDocumentCatalog();
		PDMetadata metadata = catalog.getMetadata();
		XMPMetadata xmp = null;
		if (metadata != null) {
			xmp = XMPMetadata.load(metadata.createInputStream());
		}
		if (xmp == null) {
			xmp = new XMPMetadata();
		}
		// XMP Basic
		XMPSchemaBasic bi = xmp.getBasicSchema();
		if (bi == null) {
			bi = xmp.addBasicSchema();
		}
		if (bi.getAdvisories() != null) {
			for (String a : bi.getAdvisories()) {
				bi.removeAdvisory(a);
			}
		}
		if (xmpBasic.advisories != null) {
			for (String a : xmpBasic.advisories) {
				bi.addAdvisory(a);
			}
		}
		bi.setBaseURL(xmpBasic.baseURL);
		if (xmpBasic.createDate != null) {
			bi.setCreateDate(xmpBasic.createDate);
		}
		if (xmpBasic.modifyDate != null) {
			bi.setModifyDate(xmpBasic.modifyDate);
		}
		bi.setCreatorTool(xmpBasic.creatorTool);
		if (bi.getIdentifiers() != null) {
			for (String i : bi.getIdentifiers()) {
				bi.removeIdentifier(i);
			}
		}
		if (xmpBasic.identifiers != null) {
			for (String i : xmpBasic.identifiers) {
				bi.addIdentifier(i);
			}
		}
		bi.setLabel(xmpBasic.label);
		if (xmpBasic.metadataDate != null) {
			bi.setMetadataDate(xmpBasic.metadataDate);
		}
		bi.setNickname(xmpBasic.nickname);
		bi.setRating(xmpBasic.rating);
		bi.setTitle(xmpBasic.title);
		// XMP PDF
		XMPSchemaPDF pi = xmp.getPDFSchema();
		if (pi == null) {
			pi = xmp.addPDFSchema();
		}
		pi.setKeywords(xmpPdf.keywords);
		pi.setProducer(xmpPdf.producer);
		// XMP Dublin Core
		XMPSchemaDublinCore dc = xmp.getDublinCoreSchema();
		if (dc == null) {
			dc = xmp.addDublinCoreSchema();
		}
		dc.setTitle(xmpDc.title);
		//
		if (dc.getContributors() != null) {
			for (String i : dc.getContributors()) {
				dc.removeContributor(i);
			}
		}
		if (xmpDc.contributors != null) {
			for (String i : xmpDc.contributors) {
				dc.addContributor(i);
			}
		}
		//
		if (dc.getPublishers() != null) {
			for (String i : dc.getPublishers()) {
				dc.removePublisher(i);
			}
		}
		if (xmpDc.publishers != null) {
			for (String i : xmpDc.publishers) {
				dc.addPublisher(i);
			}
		}
		//
		if (dc.getRelationships() != null) {
			for (String i : dc.getRelationships()) {
				dc.removeRelation(i);
			}
		}
		if (xmpDc.relationships != null) {
			for (String i : xmpDc.relationships) {
				dc.addRelation(i);
			}
		}
		//
		if (dc.getSubjects() != null) {
			for (String i : dc.getSubjects()) {
				dc.removeSubject(i);
			}
		}
		if (xmpDc.subjects != null) {
			for (String i : xmpDc.subjects) {
				dc.addSubject(i);
			}
		}
		// TODO: Why not remove first?
		// for(String i: dc.getTypes()){
		// dc.removeType(i);
		// }
		if (xmpDc.types != null) {
			for (String i : xmpDc.types) {
				dc.addType(i);
			}
		}
		//
		if (dc.getLanguages() != null) {
			for (String i : dc.getLanguages()) {
				dc.removeLanguage(i);
			}
		}
		if (xmpDc.languages != null) {
			for (String i : xmpDc.languages) {
				dc.addLanguage(i);
			}
		}
		//
		if (dc.getCreators() != null) {
			for (String i : dc.getCreators()) {
				dc.removeCreator(i);
			}
		}
		if (xmpDc.creators != null) {
			for (String i : xmpDc.creators) {
				dc.addCreator(i);
			}
		}
		//
		dc.setCoverage(xmpDc.coverage);
		dc.setFormat(xmpDc.format);
		dc.setIdentifier(xmpDc.identifier);
		dc.setRights(xmpDc.rights);
		dc.setSource(xmpDc.source);
		dc.setDescription(xmpDc.description);
		// xmpDcDates.setText(itemListToText(dc.getDates(),","));

		// Do the save
		PDMetadata metadataStream = new PDMetadata(document);
		try {
			metadataStream.importXMPMetadata(xmp);
		} catch (TransformerException e) {
			throw new Exception("Failed to save document:" + e.getMessage());
		}
		catalog.setMetadata(metadataStream);
		try {
			document.save(pdfFile.getAbsolutePath());
		} catch (COSVisitorException e) {
			throw new Exception("Failed to save document:" + e.getMessage());
		}

	}

	public void saveToPDF(File pdfFile) throws Exception {
		PDDocument document = null;

		document = PDDocument.load(new FileInputStream(pdfFile));

		saveToPDF(document, pdfFile);

		if (document != null) {
			try {
				document.close();
			} catch (Exception e) {

			}
		}

	}

	public void copyBasicToXMP() {
		xmpPdf.keywords = basic.keywords;
		xmpPdf.producer = basic.producer;

		xmpBasic.creatorTool = basic.creator;

		xmpDc.title = basic.title;
		xmpDc.description = basic.subject;
		xmpDc.creators = Arrays.asList(new String[] { basic.author });
	}

	public void copyXMPToBasic() {
		basic.keywords = xmpPdf.keywords;
		basic.producer = xmpPdf.producer;

		basic.creator = xmpBasic.creatorTool;

		basic.title = xmpDc.title;
		basic.subject = xmpDc.description;
		basic.author = xmpDc.creators.toString().replaceAll("\\[|\\]", "")
				.replaceAll(", ", " ");

	}

	private <T> T formatItem(T s) {
		return s;
	}

	private <T> String itemListToText(List<T> slist, String separator) {
		if (slist == null) {
			return null;
		}
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < slist.size(); ++i) {
			b.append(formatItem(slist.get(i)));
			if (i < (slist.size() - 1)) {
				b.append(separator);
			}
		}
		return b.toString();
	}

	private String stringListToText(List<String> slist) {
		return itemListToText(slist, "\", \"");
	}

	public Map<String, Object> asFlatMap() {
		return asFlatMap(new Function<Object, Object>() {
			@Override
			public Object apply(Object t) {
				return t;
			}
		});
	}

	public Map<String, String> asFlatStringMap() {
		return asFlatMap(new Function<Object, String>() {
			@Override
			public String apply(Object t) {
				return t != null ? t.toString() : "";
			}
		});
	}

	protected Object getObject(String id) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (id.length() == 0 ) return null;
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		while (st.hasMoreTokens()) {
			Field group;
			group = current.getClass().getField(st.nextToken());
			current = group.get(current);

		}
		return current;
	}
	
	public Object get(String id) {
		try {
			return getObject(id);
		} catch (NoSuchFieldException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		} catch (SecurityException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		} catch (IllegalArgumentException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			System.err.println("Metadata.get(" + id + ")");
			e.printStackTrace();
			return null;
		}
	
	}
	
	public String getString(String id){
		try {
			Object o = getObject(id);
			if (o != null){
				if (o instanceof List<?>) {
					return ",".join(",", ((List<String>) o));
				}
				if (o instanceof Calendar) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					return sdf.format(((Calendar)o).getTime());
				}
				return o.toString();
			}
		} catch (NoSuchFieldException e) {
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		} catch (IllegalAccessException e) {
		}
		return null;
	
	}

	public void set(String id, Object value) {
		StringTokenizer st = new StringTokenizer(id, ".");
		Object current = this;
		while (current != null) {
			try {
				Field field = current.getClass().getField(st.nextToken());
				if (!st.hasMoreTokens()) {
					field.set(current, value);
					return;
				}
				current = field.get(current);
			} catch (NoSuchFieldException e) {
				System.err.println("Metadata.set(" + id + ", "
						+ value.toString() + ")");
				e.printStackTrace();
				return;
			} catch (SecurityException e) {
				System.err.println("Metadata.set(" + id + ", "
						+ value.toString() + ")");
				e.printStackTrace();
				return;
			} catch (IllegalArgumentException e) {
				System.err.println("Metadata.set(" + id + ", "
						+ value.toString() + ")");
				e.printStackTrace();
				return;
			} catch (IllegalAccessException e) {
				System.err.println("Metadata.set(" + id + ", "
						+ value.toString() + ")");
				e.printStackTrace();
				return;
			}

		}
	}

	public List<String> keys(){
		ArrayList<String> ret = new ArrayList<String>();
		for (String fieldName : new String[] { "basic", "xmpBasic", "xmpPdf",
		"xmpDc" }) {
			Field group;
			Object groupObj;
			try {
				Field[] f = this.getClass().getFields();
				group = this.getClass().getField(fieldName);
				groupObj = group.get(this);
			} catch (NoSuchFieldException e) {
				continue;
			} catch (SecurityException e) {
				continue;
			} catch (IllegalArgumentException e) {
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}
			Class klass = group.getType();
			for (Field field : klass.getFields()) {
				try {
					ret.add( fieldName + "." + field.getName() );
				} catch (IllegalArgumentException e) {
				}
			}
		}
		return ret;
	}
	
	public <T> Map<String, T> asFlatMap(Function<Object, T> convertor) {
		LinkedHashMap<String, T> map = new LinkedHashMap<String, T>();

		for (String fieldName : new String[] { "basic", "xmpBasic", "xmpPdf",
				"xmpDc" }) {
			Field group;
			Object groupObj;
			try {
				Field[] f = this.getClass().getFields();
				group = this.getClass().getField(fieldName);
				groupObj = group.get(this);
			} catch (NoSuchFieldException e) {
				continue;
			} catch (SecurityException e) {
				continue;
			} catch (IllegalArgumentException e) {
				continue;
			} catch (IllegalAccessException e) {
				continue;
			}
			Class klass = group.getType();
			for (Field field : klass.getFields()) {
				try {
					Object o = field.get(groupObj);
					/*
					 * if(o instanceof List<?>){ o =
					 * stringListToText((List<String>) o); } if( o instanceof
					 * String && ( ((String)o).indexOf('\n') != -1) ){ o =
					 * ((String)o).replace("\n", "\\n" ) ; }
					 */
					map.put(fieldName + "." + field.getName(),
							convertor.apply(o));
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
		}
		return map;
	}

	public void  fromFlatMap(Map<String, Object> map, Function<Object, Object> convertor) {
		for (String fieldName : keys()) {
			if(map.containsKey(fieldName)){
				set(fieldName, convertor.apply(map.get(fieldName)));
			}
		}
	}

	public void copyFrom(MetadataInfo other){
		for (String fieldName : keys()) {
			set(fieldName, other.get(fieldName));
		}
		
	}
	
	public String toYAML() {
		DumperOptions options = new DumperOptions();
		// options.setCanonical(true);
		// options.setDefaultScalarStyle(ScalarStyle.PLAIN);
		// options.setDefaultFlowStyle(FlowStyle.BLOCK);
		options.setWidth(0xFFFF);

		Yaml yaml = new Yaml(options);
		return yaml.dump(asFlatMap());
	}

	public void fromYAML(String yamlString) {
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
		fromFlatMap(map, new Function<Object, Object>() {
			@Override
			public Object apply(Object t) {
				if(t instanceof Date){
					  Calendar cal = Calendar.getInstance();
					  cal.setTime((Date)t);
					  return cal;				
				}
				return t;
			}
		});
	}
	
	public static MetadataInfo getSampleMetadata(){
		MetadataInfo md = new MetadataInfo();
		// Spec is at : http://partners.adobe.com/public/developer/en/xmp/sdk/XMPspecification.pdf
		md.basic.title = "Dracula"; 
		md.basic.author = "Bram Stoker";
		md.basic.subject = "Horror tales, Epistolary fiction, Gothic fiction (Literary genre), Vampires -- Fiction, Dracula, Count (Fictitious character) -- Fiction, Transylvania (Romania) -- Fiction, Whitby (England) -- Fiction"; 
		md.basic.keywords = "Horror, Gothic, Vampires";
		md.basic.creator = "Adobe InDesign CS4 (6.0.6)";
		md.basic.producer = "Adobe PDF Library 9.0";
		md.basic.creationDate = Calendar.getInstance();
		md.basic.modificationDate = Calendar.getInstance(); 
		md.basic.trapped = "True";

		md.xmpBasic.creatorTool = "Adobe InDesign CS4 (6.0.6)";
		md.xmpBasic.createDate = md.basic.creationDate;
		md.xmpBasic.modifyDate = md.basic.modificationDate;
		md.xmpBasic.title = md.basic.title;
		md.xmpBasic.baseURL = "https://www.gutenberg.org/";
		md.xmpBasic.rating = 3; 
		md.xmpBasic.label = "Horror Fiction Collection";
		md.xmpBasic.nickname = "dracula";
		md.xmpBasic.identifiers = Arrays.asList("Dracula_original_edition");
		//md.xmpBasic.advisories ; 
		md.xmpBasic.metadataDate = Calendar.getInstance();

		md.xmpPdf.pdfVersion = "1.5"; 
		md.xmpPdf.keywords = md.basic.keywords;
		md.xmpPdf.producer = "Adobe PDF Library 9.0";

		md.xmpDc.title = md.basic.title;
		md.xmpDc.description = "The famous Bram Stocker book"; 
		md.xmpDc.creators = new ArrayList<String>();
		md.xmpDc.creators.add("Bram Stocker");
		md.xmpDc.subjects = Arrays.asList(md.basic.subject.split("\\s*,\\s*"));
		
		/*
		md.xmpDc.contributors;
		md.xmpDc.coverage 
		md.xmpDc.dates = 
		md.xmpDc.format = 
		md.xmpDc.identifier = 
		md.xmpDc.languages = 
		md.xmpDc.publishers = 
		md.xmpDc.relationships = 
		md.xmpDc.rights = 
		md.xmpDc.source = 
		md.xmpDc.types = 
		*/
		return md;
	}


}
