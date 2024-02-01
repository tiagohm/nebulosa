export const HIPS_SURVEY_TYPES = [
    'CDS_P_DSS2_NIR',
    'CDS_P_DSS2_BLUE', 'CDS_P_DSS2_COLOR',
    'CDS_P_DSS2_RED', 'FZU_CZ_P_CTA_FRAM_SURVEY_B',
    'FZU_CZ_P_CTA_FRAM_SURVEY_R', 'FZU_CZ_P_CTA_FRAM_SURVEY_V',
    'FZU_CZ_P_CTA_FRAM_SURVEY_COLOR', 'CDS_P_2MASS_H',
    'CDS_P_2MASS_J', 'CDS_P_2MASS_K',
    'CDS_P_2MASS_COLOR', 'CDS_P_AKARI_FIS_COLOR',
    'CDS_P_AKARI_FIS_N160', 'CDS_P_AKARI_FIS_N60',
    'CDS_P_AKARI_FIS_WIDEL', 'CDS_P_AKARI_FIS_WIDES',
    'CDS_P_NEOWISER_COLOR', 'CDS_P_NEOWISER_W1',
    'CDS_P_NEOWISER_W2', 'CDS_P_WISE_WSSA_12UM',
    'CDS_P_ALLWISE_W1', 'CDS_P_ALLWISE_W2',
    'CDS_P_ALLWISE_W3', 'CDS_P_ALLWISE_W4',
    'CDS_P_ALLWISE_COLOR', 'CDS_P_UNWISE_W1',
    'CDS_P_UNWISE_W2', 'CDS_P_UNWISE_COLOR_W2_W1W2_W1',
    'CDS_P_RASS', 'JAXA_P_ASCA_GIS',
    'JAXA_P_ASCA_SIS', 'JAXA_P_MAXI_GSC',
    'JAXA_P_MAXI_SSC', 'JAXA_P_SUZAKU',
    'JAXA_P_SWIFT_BAT_FLUX', 'CDS_P_EGRET_DIF_100_150',
    'CDS_P_EGRET_DIF_1000_2000', 'CDS_P_EGRET_DIF_150_300',
    'CDS_P_EGRET_DIF_2000_4000', 'CDS_P_EGRET_DIF_30_50',
    'CDS_P_EGRET_DIF_300_500', 'CDS_P_EGRET_DIF_4000_10000',
    'CDS_P_EGRET_DIF_50_70', 'CDS_P_EGRET_DIF_500_1000',
    'CDS_P_EGRET_DIF_70_100', 'CDS_P_EGRET_INF100',
    'CDS_P_EGRET_SUP100', 'CDS_P_FERMI_3',
    'CDS_P_FERMI_4', 'CDS_P_FERMI_5', 'CDS_P_FERMI_COLOR'
] as const

export type HipsSurveyType = (typeof HIPS_SURVEY_TYPES)[number]

export interface HipsSurvey {
    type: HipsSurveyType | string
    id: string
    category: string
    frame: string
    regime: string
    bitPix: number
    pixelScale: number
    skyFraction: number
}