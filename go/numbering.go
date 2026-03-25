package numberingmaker

import (
	"regexp"
	"strconv"
	"strings"
)

type MultiLevelType string

const (
	SingleLevel      MultiLevelType = "singleLevel"
	MultiLevel       MultiLevelType = "multilevel"
	HybridMultilevel MultiLevelType = "hybridMultilevel"
)

type NumberingFormat string

const (
	DecimalFormat            NumberingFormat = "decimal"
	UpperLetterFormat        NumberingFormat = "upperLetter"
	LowerLetterFormat        NumberingFormat = "lowerLetter"
	UpperRomanFormat         NumberingFormat = "upperRoman"
	LowerRomanFormat         NumberingFormat = "lowerRoman"
	OrdinalFormat            NumberingFormat = "ordinal"
	BulletFormat             NumberingFormat = "bullet"
	ChineseSimplifiedFormat  NumberingFormat = "chineseSimplified"
	ChineseTraditionalFormat NumberingFormat = "chineseTraditional"
)

type NumberingLevelConfig struct {
	Start   *int
	Level   *int
	Pattern *string
	Suffix  *string
}

type NumberingLevelState struct {
	Value   int  `json:"value"`
	Running bool `json:"running"`
	Changed bool `json:"changed"`
}

type NumberingMakerState struct {
	LevelStates []NumberingLevelState `json:"levelStates"`
}

type Level interface {
	GenerateNumber(values []string) string
	GetValueLabel() string
	GetLevel() int
	GetState() NumberingLevelState
	SetState(NumberingLevelState)
	Reset()
	UpdateConfig(NumberingLevelConfig)
	SetFormat(NumberingFormat)
	SetStart(int)
	SetLevel(int)
	SetPattern(string)
	GetPattern() string
	GetStart() int
	GetValue() int
	IsChanged() bool
}

type BaseLevel struct {
	Format  NumberingFormat
	Start   int
	Level   int
	Pattern string
	Suffix  string
	Value   int
	Running bool
	Changed bool
}

func (l *BaseLevel) GetValueLabel() string {
	return strconv.Itoa(l.Value)
}

func (l *BaseLevel) GetLevel() int {
	return l.Level
}

func (l *BaseLevel) GetState() NumberingLevelState {
	return NumberingLevelState{
		Value:   l.Value,
		Running: l.Running,
		Changed: l.Changed,
	}
}

func (l *BaseLevel) SetState(state NumberingLevelState) {
	l.Value = state.Value
	l.Running = state.Running
	l.Changed = state.Changed
}

func (l *BaseLevel) Reset() {
	l.Value = l.Start
	l.Running = false
	l.Changed = false
}

func (l *BaseLevel) UpdateConfig(config NumberingLevelConfig) {
	if config.Start != nil {
		l.SetStart(*config.Start)
	}
	if config.Level != nil {
		l.Level = *config.Level
	}
	if config.Pattern != nil {
		l.Pattern = *config.Pattern
	}
	if config.Suffix != nil {
		l.Suffix = *config.Suffix
	}
}

func (l *BaseLevel) SetFormat(format NumberingFormat) {
	l.Format = format
}

func (l *BaseLevel) SetStart(start int) {
	l.Start = start
	l.Value = start
}

func (l *BaseLevel) SetLevel(level int) {
	l.Level = level
}

func (l *BaseLevel) SetPattern(pattern string) {
	l.Pattern = pattern
}

func (l *BaseLevel) GetPattern() string {
	return l.Pattern
}

func (l *BaseLevel) GetStart() int {
	return l.Start
}

func (l *BaseLevel) GetValue() int {
	return l.Value
}

func (l *BaseLevel) IsChanged() bool {
	return l.Changed
}

type DecimalLevel struct {
	BaseLevel
}

func (l *DecimalLevel) GenerateNumber(values []string) string {
	label := l.GetValueLabel()
	if l.Running {
		l.Value++
		l.Changed = true
		label = l.GetValueLabel()
	}
	l.Running = true
	values[l.Level] = label
	output := l.Pattern
	for i, value := range values {
		output = strings.ReplaceAll(output, "%"+strconv.Itoa(i+1), value)
	}
	return output
}

type BulletLevel struct {
	BaseLevel
}

func (l *BulletLevel) GenerateNumber(values []string) string {
	label := l.GetValueLabel()
	if l.Running {
		l.Value++
		l.Changed = true
		label = l.GetValueLabel()
	}
	l.Running = true
	values[l.Level] = label
	output := l.Pattern
	for i, value := range values {
		output = strings.ReplaceAll(output, "%"+strconv.Itoa(i+1), value)
	}
	return output
}

func (l *BulletLevel) GetValueLabel() string {
	return ""
}

type LetterLevel struct {
	BaseLevel
	lowerCase bool
}

func (l *LetterLevel) GenerateNumber(values []string) string {
	label := l.GetValueLabel()
	if l.Running {
		l.Value++
		l.Changed = true
		label = l.GetValueLabel()
	}
	l.Running = true
	values[l.Level] = label
	output := l.Pattern
	for i, value := range values {
		output = strings.ReplaceAll(output, "%"+strconv.Itoa(i+1), value)
	}
	return output
}

func (l *LetterLevel) GetValueLabel() string {
	base := 'A'
	if l.lowerCase {
		base = 'a'
	}
	value := l.Value
	var builder []rune
	for value > 0 {
		value--
		remainder := value % 26
		builder = append([]rune{rune(int(base) + remainder)}, builder...)
		value /= 26
	}
	return string(builder)
}

type RomanLevel struct {
	BaseLevel
	lowerCase bool
}

func (l *RomanLevel) GenerateNumber(values []string) string {
	label := l.GetValueLabel()
	if l.Running {
		l.Value++
		l.Changed = true
		label = l.GetValueLabel()
	}
	l.Running = true
	values[l.Level] = label
	output := l.Pattern
	for i, value := range values {
		output = strings.ReplaceAll(output, "%"+strconv.Itoa(i+1), value)
	}
	return output
}

func (l *RomanLevel) GetValueLabel() string {
	values := []int{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1}
	upperSymbols := []string{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"}
	lowerSymbols := []string{"m", "cm", "d", "cd", "c", "xc", "l", "xl", "x", "ix", "v", "iv", "i"}
	symbols := upperSymbols
	if l.lowerCase {
		symbols = lowerSymbols
	}
	num := l.Value
	var builder strings.Builder
	for i, value := range values {
		for num >= value {
			num -= value
			builder.WriteString(symbols[i])
		}
	}
	return builder.String()
}

type OrdinalLevel struct {
	BaseLevel
}

func (l *OrdinalLevel) GenerateNumber(values []string) string {
	label := l.GetValueLabel()
	if l.Running {
		l.Value++
		l.Changed = true
		label = l.GetValueLabel()
	}
	l.Running = true
	values[l.Level] = label
	output := l.Pattern
	for i, value := range values {
		output = strings.ReplaceAll(output, "%"+strconv.Itoa(i+1), value)
	}
	return output
}

func (l *OrdinalLevel) GetValueLabel() string {
	value := l.Value
	mod100 := value % 100
	if mod100 >= 11 && mod100 <= 13 {
		return strconv.Itoa(value) + "th"
	}
	switch value % 10 {
	case 1:
		return strconv.Itoa(value) + "st"
	case 2:
		return strconv.Itoa(value) + "nd"
	case 3:
		return strconv.Itoa(value) + "rd"
	default:
		return strconv.Itoa(value) + "th"
	}
}

type ChineseLevel struct {
	BaseLevel
	traditional bool
}

func (l *ChineseLevel) GenerateNumber(values []string) string {
	label := l.GetValueLabel()
	if l.Running {
		l.Value++
		l.Changed = true
		label = l.GetValueLabel()
	}
	l.Running = true
	values[l.Level] = label
	output := l.Pattern
	for i, value := range values {
		output = strings.ReplaceAll(output, "%"+strconv.Itoa(i+1), value)
	}
	return output
}

func (l *ChineseLevel) GetValueLabel() string {
	return convertToChinese(l.Value, l.traditional)
}

func CreateLevel(format NumberingFormat, config *NumberingLevelConfig) Level {
	var level Level
	switch format {
	case DecimalFormat:
		level = &DecimalLevel{}
	case UpperLetterFormat:
		level = &LetterLevel{lowerCase: false}
	case LowerLetterFormat:
		level = &LetterLevel{lowerCase: true}
	case UpperRomanFormat:
		level = &RomanLevel{lowerCase: false}
	case LowerRomanFormat:
		level = &RomanLevel{lowerCase: true}
	case OrdinalFormat:
		level = &OrdinalLevel{}
	case BulletFormat:
		level = &BulletLevel{}
	case ChineseSimplifiedFormat:
		level = &ChineseLevel{traditional: false}
	case ChineseTraditionalFormat:
		level = &ChineseLevel{traditional: true}
	default:
		level = &DecimalLevel{}
	}
	level.SetFormat(format)
	if config != nil {
		level.UpdateConfig(*config)
	}
	return level
}

type NumberingMaker struct {
	NumID          int
	MultiLevelType MultiLevelType
	Levels         []Level
}

func (m *NumberingMaker) GenerateNumbering(level int) string {
	if level < 0 || level >= len(m.Levels) {
		return ""
	}
	values := make([]string, len(m.Levels))
	for i, current := range m.Levels {
		values[i] = current.GetValueLabel()
	}
	current := m.Levels[level]
	result := current.GenerateNumber(values)
	if current.IsChanged() {
		for i := level + 1; i < len(m.Levels); i++ {
			m.Levels[i].Reset()
		}
	}
	return result
}

func (m *NumberingMaker) GetLevel(level int) Level {
	if level < 0 || level >= len(m.Levels) {
		return nil
	}
	return m.Levels[level]
}

func (m *NumberingMaker) GetState() NumberingMakerState {
	state := NumberingMakerState{
		LevelStates: make([]NumberingLevelState, 0, len(m.Levels)),
	}
	for _, level := range m.Levels {
		state.LevelStates = append(state.LevelStates, level.GetState())
	}
	return state
}

func (m *NumberingMaker) SetState(state NumberingMakerState) {
	size := len(state.LevelStates)
	if size > len(m.Levels) {
		size = len(m.Levels)
	}
	for i := 0; i < size; i++ {
		m.Levels[i].SetState(state.LevelStates[i])
	}
}

func CreateMultilevelNumberingMaker(numID int) *NumberingMaker {
	maker := &NumberingMaker{
		NumID:          numID,
		MultiLevelType: MultiLevel,
		Levels:         make([]Level, 0, 9),
	}
	pattern := ""
	for i := 0; i < 9; i++ {
		level := &DecimalLevel{}
		level.SetFormat(DecimalFormat)
		level.SetLevel(i)
		level.SetStart(1)
		pattern += "%" + strconv.Itoa(i+1) + "."
		level.SetPattern(pattern)
		maker.Levels = append(maker.Levels, level)
	}
	return maker
}

func convertToChinese(number int, traditional bool) string {
	simplifiedDigits := []string{"零", "一", "二", "三", "四", "五", "六", "七", "八", "九"}
	traditionalDigits := []string{"零", "壹", "貳", "叁", "肆", "伍", "陸", "柒", "捌", "玖"}
	simplifiedUnits := []string{"", "十", "百", "千", "万", "十", "百", "千", "亿"}
	traditionalUnits := []string{"", "拾", "佰", "仟", "萬", "拾", "佰", "仟", "億"}
	digits := simplifiedDigits
	units := simplifiedUnits
	if traditional {
		digits = traditionalDigits
		units = traditionalUnits
	}
	if number == 0 {
		return digits[0]
	}
	parts := make([]string, 0)
	unitPos := 0
	needZero := false
	for number > 0 {
		digit := number % 10
		if digit == 0 {
			if len(parts) > 0 && !needZero {
				parts = append([]string{digits[0]}, parts...)
			}
			needZero = true
		} else {
			current := digits[digit] + units[unitPos]
			if needZero {
				parts = append([]string{digits[0]}, parts...)
				needZero = false
			}
			parts = append([]string{current}, parts...)
		}
		number /= 10
		unitPos++
	}
	result := strings.Join(parts, "")
	if !traditional && strings.HasPrefix(result, "一十") {
		result = strings.TrimPrefix(result, "一")
	}
	result = regexp.MustCompile("零{4}").ReplaceAllString(result, units[4])
	result = regexp.MustCompile("零{8}").ReplaceAllString(result, units[8])
	result = regexp.MustCompile("零{2,}").ReplaceAllString(result, "零")
	result = strings.TrimSuffix(result, "零")
	return result
}
