package org.sdrc.rani.document;

import java.util.Map;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

@Document
@Setter @Getter
public class AchievementData {
	private String _id;
	private Map<String, String> dataValue;
}
