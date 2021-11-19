package org.emunix.unipatcher.patcher;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.helpers.ResourceProvider;
import org.emunix.unipatcher.utils.UFileUtils;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

@RunWith(MockitoJUnitRunner.class)
public class IPSTest {

    private static final String NOT_IPS_PATCH = "Not an IPS patch.";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    ResourceProvider resourceProvider;

    @Mock
    Context context;

    private UFileUtils fileUtils;

    @Before
    public void setUp() throws Exception {
        when(resourceProvider.getString(R.string.notify_error_not_ips_patch))
                .thenReturn(NOT_IPS_PATCH);
        fileUtils = new UFileUtils(context, resourceProvider);
    }

    @Test
    public void IPS_InvalidPatch_NoMagic() throws Exception {
        File patch = new File(this.getClass().getResource("/ips/not_ips.ips").getPath());
        File in = new File(getClass().getResource("/ips/min_ips.bin").getPath());
        File out = folder.newFile("out.bin");

        IPS patcher = new IPS(patch, in, out, resourceProvider, fileUtils);

        try {
            patcher.apply();
            fail("Expected an PatchException to be thrown");
        } catch (PatchException e) {
            assertThat(e.getMessage(), is("Not an IPS patch."));
        }
    }

    @Test
    public void IPS_MinPatch() throws Exception {
        assertTrue(ApplyPatch("/ips/min_ips.ips", "/ips/min_ips.bin", "/ips/min_ips_modified.bin"));
    }

    @Test
    public void IPS_RlePatch() throws Exception {
        assertTrue(ApplyPatch("/ips/rle_ips.ips", "/ips/rle_ips.bin", "/ips/rle_ips_modified.bin"));
    }

    @Test
    public void IPS_ExtendPatch() throws Exception {
        assertTrue(ApplyPatch("/ips/extend_ips.ips", "/ips/extend_ips.bin", "/ips/extend_ips_modified.bin"));
    }

    @Test
    public void IPS_TruncateRom() throws Exception {
        assertTrue(ApplyPatch("/ips/truncate.ips", "/ips/truncate.bin", "/ips/truncate_modified.bin"));
    }

    @Test
    public void IPS32_MinPatch() throws Exception {
        assertTrue(ApplyPatch("/ips/min_ips32.ips", "/ips/min_ips32.bin", "/ips/min_ips32_mod.bin"));
    }

    @Test
    public void IPS32_RlePatch() throws Exception {
        assertTrue(ApplyPatch("/ips/rle_ips32.ips", "/ips/rle_ips32.bin", "/ips/rle_ips32_mod.bin"));
    }

    @Test
    public void IPS32_ExtendPatch() throws Exception {
        assertTrue(ApplyPatch("/ips/extend_ips32.ips", "/ips/extend_ips32.bin", "/ips/extend_ips32_mod.bin"));
    }

    private boolean ApplyPatch(String patchName, String origName, String modifiedName) throws Exception {
        File patch = new File(this.getClass().getResource(patchName).getPath());
        File in = new File(getClass().getResource(origName).getPath());
        File out = folder.newFile("out.bin");

        IPS patcher = new IPS(patch, in, out, resourceProvider, fileUtils);
        try {
            patcher.apply();
        } catch (PatchException | IOException e) {
            fail("Patching failed");
        }

        File origOut = new File(getClass().getResource(modifiedName).getPath());
        return fileUtils.checksumCRC32(out) == fileUtils.checksumCRC32(origOut);
    }
}