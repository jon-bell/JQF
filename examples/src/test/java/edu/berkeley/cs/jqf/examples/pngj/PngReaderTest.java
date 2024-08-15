package edu.berkeley.cs.jqf.examples.pngj;

import ar.com.hjg.pngj.PngReaderByte;
import ar.com.hjg.pngj.PngjInputException;
import com.pholser.junit.quickcheck.From;
import edu.berkeley.cs.jqf.examples.common.ByteArrayWrapper;
import edu.berkeley.cs.jqf.examples.png.PngGenerator;
import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@RunWith(JQF.class)
public class PngReaderTest {

    @Fuzz
    public void testWithGenerator(@From(PngGenerator.class) ByteArrayWrapper bytes) {
        try {
            InputStream input = new ByteArrayInputStream(bytes.getByteArray());
            PngReaderByte reader = new PngReaderByte(input);
            reader.getMetadata();
            reader.close();
        } catch (PngjInputException e) {
            Assume.assumeNoException(e);
        }
    }


}
