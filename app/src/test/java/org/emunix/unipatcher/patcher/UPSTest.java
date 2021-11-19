package org.emunix.unipatcher.patcher;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import android.content.Context;
import java.io.File;
import java.io.IOException;
import org.emunix.unipatcher.R;
import org.emunix.unipatcher.utils.FileUtils;
import org.emunix.unipatcher.helpers.ResourceProvider;
import org.junit.*;
import org.junit.rules.*;
import org.junit.runner.*;
import org.mockito.*;
import org.mockito.junit.*;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UPSTest {

    private static final String PATCH_CORRUPTED = "The patch file is corrupted.";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Mock
    ResourceProvider resourceProvider;

    @Mock
    Context context;

    private FileUtils fileUtils;

    @Before
    public void setUp() throws Exception {
        when(resourceProvider.getString(R.string.notify_error_patch_corrupted))
                .thenReturn(PATCH_CORRUPTED);
        fileUtils = new FileUtils(context, resourceProvider);
    }

    @Test
    public void testApply() throws Exception {
        assertTrue(ApplyPatch("/ups/readUpsCrc.ups", "/ups/readUpsCrc.bin", "/ups/readUpsCrc_m.bin"));
    }

    @Test
    public void testReadUpsCrc() throws Exception {
        File patch = new File(this.getClass().getResource("/ups/readUpsCrc.ups").getPath());
        UPS.UpsCrc pCrc = null;
        try {
            pCrc = UPS.readUpsCrc(patch, resourceProvider);
        } catch (PatchException e) {
            fail("Patch exception");
        }
        assertEquals(pCrc.getPatchFileCRC(), pCrc.getRealPatchCRC());
    }

    @Test
    public void testCheckMagic() throws Exception {
        File patch = new File(this.getClass().getResource("/ups/readUpsCrc.ups").getPath());
        assertTrue(UPS.checkMagic(patch));
        File noPatch = new File(this.getClass().getResource("/ups/readUpsCrc.bin").getPath());
        assertFalse(UPS.checkMagic(noPatch));
    }

    private boolean ApplyPatch(String patchName, String origName, String modifiedName) throws Exception {
        File patch = new File(this.getClass().getResource(patchName).getPath());
        File in = new File(getClass().getResource(origName).getPath());
        File out = folder.newFile("out.bin");

        UPS patcher = new UPS(patch, in, out, resourceProvider, fileUtils);
        try {
            patcher.apply(false);
        } catch (PatchException | IOException e) {
            fail("Patching failed");
        }

        File origOut = new File(getClass().getResource(modifiedName).getPath());
        return fileUtils.checksumCRC32(out) == fileUtils.checksumCRC32(origOut);
    }
}