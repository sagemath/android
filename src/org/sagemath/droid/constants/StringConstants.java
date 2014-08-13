package org.sagemath.droid.constants;

/**
 * Misc. String constants
 *
 * @author Nikhil Peter Raj
 */
public class StringConstants {

    //--BASE URLS--
    public static final String BASE_SERVER_URL = "sagecell.sagemath.org";

    //--PATHS--
    public static final String PATH_KERNEL = "kernel";
    public static final String PATH_IOPUB = "iopub";
    public static final String PATH_SHELL = "shell";
    public static final String PATH_PERMALINK = "permalink";

    //--SCHEMES--
    public static final String SCHEME_HTTPS = "https";
    public static final String SCHEME_HTTP = "http";

    public static final String ARG_PROGRESS_START = "progress-start";
    public static final String ARG_PROGRESS_END = "progress-end";

    public static final String FILE_IMAGE_OUTPUT = "imageOutput.png";

    public static final String ID = "id";
    public static final String FLAG_INPUT_EMPTY = "inputEmptyFlag";

    public static final String MATHJAX_CONFIG = "<script type=\"text/x-mathjax-config\">\n" +
            "  MathJax.Hub.Config({tex2jax: {inlineMath: [['$','$'], ['\\\\(','\\\\)']]}});\n" +
            "</script>";
    public static final String MATHJAX_CDN = "<script type=\"text/javascript\"\n" +
            "  src=\"https://cdn.mathjax.org/mathjax/latest/MathJax.js?config=TeX-AMS-MML_HTMLorMML\">\n" +
            "</script>";
    public static final String IMAGE_STYLE = "<style>img{display: inline; height: auto; max-width: 100%;}</style>";

}
