{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "koderia-flows.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "koderia-flows.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "koderia-flows.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "koderia-flows.labels" -}}
app.kubernetes.io/name: {{ include "koderia-flows.name" . }}
helm.sh/chart: {{ include "koderia-flows.chart" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- if .Chart.AppVersion }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
{{- end }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Create active profiles property value used by Spring
*/}}
{{- define "koderia-flows.activeProfiles" -}}
{{- join "," .Values.activeProfiles -}}
{{- end -}}

{{/**/}}
{{/*Create extra application properties used by Spring*/}}
{{/**/}}
{{/*{{- define "koderia-flows.extraProperties" -}}*/}}
{{/*{{- range $key, $value := .Values.configuration.properties -}}*/}}
{{/*{{- printf "--%s=%s\n" $key $value -}}*/}}
{{/*{{- end -}}*/}}
{{/*{{- end -}}*/}}
