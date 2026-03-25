package numberingmaker

import "testing"

func TestCreateMultilevelNumberingMakerHasNineLevels(t *testing.T) {
	maker := CreateMultilevelNumberingMaker(1)

	if len(maker.Levels) != 9 {
		t.Fatalf("expected 9 levels, got %d", len(maker.Levels))
	}
	if maker.MultiLevelType != MultiLevel {
		t.Fatalf("expected multi level type, got %s", maker.MultiLevelType)
	}
}

func TestGenerateNumberingAndRestoreState(t *testing.T) {
	maker1 := CreateMultilevelNumberingMaker(1)

	assertEqual(t, "1.", maker1.GenerateNumbering(0))
	assertEqual(t, "1.1.", maker1.GenerateNumbering(1))
	assertEqual(t, "1.2.", maker1.GenerateNumbering(1))
	assertEqual(t, "1.2.1.", maker1.GenerateNumbering(2))
	assertEqual(t, "1.2.2.", maker1.GenerateNumbering(2))

	state := maker1.GetState()
	if len(state.LevelStates) != 9 {
		t.Fatalf("expected 9 level states, got %d", len(state.LevelStates))
	}

	maker2 := CreateMultilevelNumberingMaker(1)
	maker2.SetState(state)

	assertEqual(t, "1.2.3.", maker1.GenerateNumbering(2))
	assertEqual(t, "1.2.3.", maker2.GenerateNumbering(2))
	assertEqual(t, "1.3.", maker1.GenerateNumbering(1))
	assertEqual(t, "1.3.", maker2.GenerateNumbering(1))
	assertEqual(t, "1.3.1.", maker1.GenerateNumbering(2))
	assertEqual(t, "1.3.1.", maker2.GenerateNumbering(2))
}

func TestGenerateNumberingOutOfRangeReturnsEmptyString(t *testing.T) {
	maker := CreateMultilevelNumberingMaker(1)

	assertEqual(t, "", maker.GenerateNumbering(-1))
	assertEqual(t, "", maker.GenerateNumbering(9))
}

func TestRowNumberRollbackSequence(t *testing.T) {
	maker := CreateMultilevelNumberingMaker(1)

	assertEqual(t, "1.", maker.GenerateNumbering(0))
	assertEqual(t, "1.1.", maker.GenerateNumbering(1))
	assertEqual(t, "1.2.", maker.GenerateNumbering(1))
	assertEqual(t, "1.2.1.", maker.GenerateNumbering(2))
	assertEqual(t, "1.2.2.", maker.GenerateNumbering(2))
	assertEqual(t, "1.2.3.", maker.GenerateNumbering(2))
	assertEqual(t, "1.3.", maker.GenerateNumbering(1))
	assertEqual(t, "1.3.1.", maker.GenerateNumbering(2))
}

func TestReadmeMixedEncoderExampleProducesExpectedSequence(t *testing.T) {
	maker := CreateMultilevelNumberingMaker(1)

	maker.Levels[0] = CreateLevel(DecimalFormat, &NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(0),
		Pattern: stringPtr("%1."),
	})
	maker.Levels[1] = CreateLevel(UpperRomanFormat, &NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(1),
		Pattern: stringPtr("%1.%2."),
	})
	maker.Levels[2] = CreateLevel(ChineseSimplifiedFormat, &NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(2),
		Pattern: stringPtr("%1.%2.%3."),
	})

	assertEqual(t, "1.", maker.GenerateNumbering(0))
	assertEqual(t, "1.I.", maker.GenerateNumbering(1))
	assertEqual(t, "1.II.", maker.GenerateNumbering(1))
	assertEqual(t, "1.II.一.", maker.GenerateNumbering(2))
	assertEqual(t, "1.II.二.", maker.GenerateNumbering(2))
}

func TestCreateLevelReturnsConcreteImplementations(t *testing.T) {
	config := &NumberingLevelConfig{
		Start:   intPtr(1),
		Level:   intPtr(0),
		Pattern: stringPtr("%1."),
	}

	if _, ok := CreateLevel(DecimalFormat, config).(*DecimalLevel); !ok {
		t.Fatal("expected decimal level")
	}
	if _, ok := CreateLevel(UpperLetterFormat, config).(*LetterLevel); !ok {
		t.Fatal("expected letter level")
	}
	if _, ok := CreateLevel(LowerRomanFormat, config).(*RomanLevel); !ok {
		t.Fatal("expected roman level")
	}
	if _, ok := CreateLevel(OrdinalFormat, config).(*OrdinalLevel); !ok {
		t.Fatal("expected ordinal level")
	}
	if _, ok := CreateLevel(BulletFormat, config).(*BulletLevel); !ok {
		t.Fatal("expected bullet level")
	}
	if _, ok := CreateLevel(ChineseTraditionalFormat, config).(*ChineseLevel); !ok {
		t.Fatal("expected chinese level")
	}
}

func TestConversionsCoverTypicalAndBoundaryValues(t *testing.T) {
	upperLetter := CreateLevel(UpperLetterFormat, &NumberingLevelConfig{Start: intPtr(26), Level: intPtr(0), Pattern: stringPtr("%1")})
	lowerRoman := CreateLevel(LowerRomanFormat, &NumberingLevelConfig{Start: intPtr(58), Level: intPtr(0), Pattern: stringPtr("%1")})
	ordinal := CreateLevel(OrdinalFormat, &NumberingLevelConfig{Start: intPtr(11), Level: intPtr(0), Pattern: stringPtr("%1")})
	chineseSimplified := CreateLevel(ChineseSimplifiedFormat, &NumberingLevelConfig{Start: intPtr(10), Level: intPtr(0), Pattern: stringPtr("%1")})
	chineseTraditional := CreateLevel(ChineseTraditionalFormat, &NumberingLevelConfig{Start: intPtr(1234), Level: intPtr(0), Pattern: stringPtr("%1")})
	bullet := CreateLevel(BulletFormat, &NumberingLevelConfig{Start: intPtr(1), Level: intPtr(0), Pattern: stringPtr("%1")})

	assertEqual(t, "Z", upperLetter.GetValueLabel())
	assertEqual(t, "lviii", lowerRoman.GetValueLabel())
	assertEqual(t, "11th", ordinal.GetValueLabel())
	assertEqual(t, "十", chineseSimplified.GetValueLabel())
	assertEqual(t, "壹仟貳佰叁拾肆", chineseTraditional.GetValueLabel())
	assertEqual(t, "", bullet.GetValueLabel())
}

func assertEqual(t *testing.T, expected string, actual string) {
	t.Helper()
	if expected != actual {
		t.Fatalf("expected %q, got %q", expected, actual)
	}
}

func intPtr(value int) *int {
	return &value
}

func stringPtr(value string) *string {
	return &value
}
