package org.sagemath.droid.models.gson;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * The class used to construct requests from the input.
 *
 * @author Nikhil Peter Raj
 */
public class Request implements Parcelable {
    private static final String EXECUTE_REQUEST = "execute_request";
    //private static final String

    private Header header;
    private Header parent_header;
    private RequestContent content;
    private MetaData metadata;

    public Request() {
        header = new Header();
        parent_header = new Header();
        content = new RequestContent();
        metadata = new MetaData();
    }

    public Request(String sageInput) {
        this();
        getHeader().init(); // With random UUID
        getHeader().setMessageType(EXECUTE_REQUEST);
        getContent().setCode(sageInput);
    }

    public Request(String sageInput, String session) {
        this();
        getHeader().init(session);
        getHeader().setMessageType(EXECUTE_REQUEST);
        getContent().setCode(sageInput);

    }

    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Header getParentHeader() {
        return parent_header;
    }

    public void setParentHeader(Header parent_header) {
        this.parent_header = parent_header;
    }

    public RequestContent getContent() {
        return content;
    }

    public void setContent(RequestContent content) {
        this.content = content;
    }

    public MetaData getMetadata() {
        return metadata;
    }

    public void setMetadata(MetaData metadata) {
        this.metadata = metadata;
    }

    public static class RequestContent implements Parcelable {

        private String code;
        private boolean silent;
        private ArrayList<String> user_variables;
        private UserExpressions user_expressions;
        private boolean allow_stdin;


        public RequestContent() {
            user_variables = new ArrayList<String>();
            user_expressions = new UserExpressions();
            setSilent(false);
            setAllowStdin(false);
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public boolean isSilent() {
            return silent;
        }

        public void setSilent(boolean silent) {
            this.silent = silent;
        }

        public ArrayList<String> getUserVariables() {
            return user_variables;
        }

        public void setUserVariables(ArrayList<String> user_variables) {
            this.user_variables = user_variables;
        }

        public boolean isAllowStdin() {
            return allow_stdin;
        }

        public void setAllowStdin(boolean allow_stdin) {
            this.allow_stdin = allow_stdin;
        }

        private RequestContent(Parcel in) {
            code = in.readString();
            silent = in.readInt() == 1;
            user_variables = (ArrayList<String>) in.readArrayList(String.class.getClassLoader());
            user_expressions = in.readParcelable(UserExpressions.class.getClassLoader());
            allow_stdin = in.readInt() == 1;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(code);
            dest.writeInt((silent ? 1 : 0));
            dest.writeList(user_variables);
            dest.writeParcelable(user_expressions, flags);
            dest.writeInt((allow_stdin ? 1 : 0));
        }

        public static final Creator<RequestContent> CREATOR = new Creator<RequestContent>() {
            @Override
            public RequestContent createFromParcel(Parcel source) {
                return new RequestContent(source);
            }

            @Override
            public RequestContent[] newArray(int size) {
                return new RequestContent[size];
            }
        };


        public static class UserExpressions implements Parcelable {
            private String _sagecell_files = "sys._sage_.new_files()";

            public UserExpressions() {

            }

            private UserExpressions(Parcel in) {
                _sagecell_files = in.readString();
            }

            public static final Creator<UserExpressions> CREATOR = new Creator<UserExpressions>() {
                @Override
                public UserExpressions createFromParcel(Parcel source) {
                    return new UserExpressions(source);
                }

                @Override
                public UserExpressions[] newArray(int size) {
                    return new UserExpressions[size];
                }
            };

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(_sagecell_files);
            }
        }
    }

    public static class MetaData {

    }

    //PARCELABLE
    private Request(Parcel in) {
        header = in.readParcelable(Header.class.getClassLoader());
        parent_header = in.readParcelable(Header.class.getClassLoader());
        content = in.readParcelable(RequestContent.class.getClassLoader());
        metadata = new MetaData();
    }

    public static final Creator<Request> CREATOR = new Creator<Request>() {
        @Override
        public Request createFromParcel(Parcel source) {
            return new Request(source);
        }

        @Override
        public Request[] newArray(int size) {
            return new Request[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(header, flags);
        dest.writeParcelable(parent_header, flags);
        dest.writeParcelable(content, flags);
    }
}
