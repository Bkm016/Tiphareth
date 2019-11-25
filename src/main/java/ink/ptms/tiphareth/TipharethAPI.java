package ink.ptms.tiphareth;

import ink.ptms.tiphareth.pack.PackDispatcher;
import ink.ptms.tiphareth.pack.PackGenerator;
import ink.ptms.tiphareth.pack.PackLoader;
import ink.ptms.tiphareth.pack.PackUploader;

/**
 * @Author sky
 * @Since 2019-11-26 7:29
 */
public class TipharethAPI {

    public static final PackLoader LOADER = PackLoader.INSTANCE;
    public static final PackUploader UPLOADER = PackUploader.INSTANCE;
    public static final PackGenerator GENERATOR = PackGenerator.INSTANCE;
    public static final PackDispatcher DISPATCHER = PackDispatcher.INSTANCE;

}
