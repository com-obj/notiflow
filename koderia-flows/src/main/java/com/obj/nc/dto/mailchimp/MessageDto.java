package com.obj.nc.dto.mailchimp;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Data
public class MessageDto {

    @NotBlank
    private String subject;

//    private String html;
//    private String text;

    @NotBlank
    private String from_email;
    @NotBlank
    private String from_name;
    @NotEmpty
    private List<RecipientDto> to;

//    private Map<String, Object> headers = new HashMap<>();

//    private boolean important = false;
//    private boolean track_opens = false;
//    private boolean track_clicks = false;
//    private boolean auto_text = false;
//    private boolean auto_html = false;
//    private boolean inline_css = false;
//    private boolean url_strip_qs = false;
//    private boolean preserve_recipients = false;
//    private boolean view_content_link = false;
//
//    private String bcc_address;
//    private String tracking_domain;
//    private String signing_domain;
//    private String return_path_domain;

    private String merge_language = "handlebars";

    private List<MergeVarDto> global_merge_vars = new ArrayList<>();
//    private List<RecipientMergeVarsDto> merge_vars = new ArrayList<>();

//    private List<String> tags = new ArrayList<>();
//    private String subaccount;
//
//    private List<String> google_analytics_domains = new ArrayList<>();
//    private String google_analytics_campaign;
//
//    private MetadataDto metadata;
//    private List<RecipientMetadataDto> recipient_metadata = new ArrayList<>();

    private List<AttachmentDto> attachments = new ArrayList<>();
//    private List<ImageDto> images = new ArrayList<>();
//
//    private boolean async = false;
//    private String ip_pool;
//    private String send_at;

}
